package com.alttd.schedulers;

import com.alttd.database.queries.QueriesReminders.QueriesReminders;
import com.alttd.database.queries.QueriesReminders.Reminder;
import com.alttd.util.Logger;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    private static ReminderScheduler instance = null;
    private final ArrayList<Reminder> reminders;
    private Reminder nextReminder;
    private final JDA jda;

    private ReminderScheduler(JDA jda) {
        instance = this;
        this.jda = jda;
        reminders = QueriesReminders.getReminders();
        if (reminders == null) {
            Logger.altitudeLogs.error("Unable to retrieve reminders");
            instance = null;
            return;
        }
        reminders.sort(Comparator.comparingLong(Reminder::remindDate));
        if (reminders.size() == 0)
            nextReminder = null;
        else
            nextReminder = reminders.get(0);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(new ReminderRun(), 0, 1, TimeUnit.MINUTES);

    }

    public static ReminderScheduler getInstance(JDA jda) {
        if (instance == null)
            instance = new ReminderScheduler(jda);
        return instance;
    }

    public synchronized void addReminder(Reminder reminder) {
        Logger.altitudeLogs.debug("Adding reminder with messageId: " + reminder.messageId());
        reminders.add(reminder);
        reminders.sort(Comparator.comparingLong(Reminder::remindDate));
        nextReminder = reminders.get(0);
    }

    public synchronized void removeReminder(Reminder reminder, boolean removeFromDatabase) {
        Logger.altitudeLogs.debug("Removing reminder with messageId: " + reminder.messageId());
        reminders.remove(reminder);
        if (reminders.size() == 0)
            nextReminder = null;
        else
            nextReminder = reminders.get(0);
        if (removeFromDatabase)
            QueriesReminders.removeReminder(reminder.id());
    }

    public synchronized void removeReminder(long messageId) {
        Logger.altitudeLogs.debug("Attempting to remove reminder with messageId: " + messageId);
        reminders.stream()
                .filter(reminder -> reminder.messageId() == messageId)
                .findAny()
                .ifPresent(reminder -> removeReminder(reminder, true));
    }

    private class ReminderRun implements Runnable {

        @Override
        public void run() {
            long time = System.currentTimeMillis();
            while (nextReminder != null && time > nextReminder.remindDate()) {
                Channel channel = nextReminder.getChannel(jda);
                if (channel == null) {
                    Logger.altitudeLogs.warning("Couldn't find channel, unable to run reminder: " + nextReminder.id() +
                            "\ntitle: [" + nextReminder.title() +
                            "]\ndescription: [" + nextReminder.description() + "]");
                    return;
                }
                sendEmbed(nextReminder, channel);
                if (nextReminder.shouldRepeat()) {
                    Reminder repeatedReminder = new Reminder(
                            nextReminder.id(),
                            nextReminder.title(),
                            nextReminder.description(),
                            nextReminder.userId(),
                            nextReminder.guildId(),
                            nextReminder.channelId(),
                            nextReminder.messageId(),
                            true,
                            nextReminder.creationDate(),
                            nextReminder.remindDate() + TimeUnit.DAYS.toMillis(1),
                            nextReminder.reminderType(),
                            nextReminder.data());
                    removeReminder(nextReminder, false);
                    addReminder(repeatedReminder);
                    QueriesReminders.updateReminderDate(nextReminder.remindDate() + TimeUnit.DAYS.toMillis(1), nextReminder.id());
                }
                else
                    removeReminder(nextReminder, true);
            }
        }
        private void sendEmbed(Reminder reminder, Channel channel) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(reminder.title())
                    .setDescription(reminder.description())
                    .appendDescription("\n\nRequested <t:" + TimeUnit.MILLISECONDS.toSeconds(reminder.creationDate()) + ":R>");
            Guild guild = reminder.getGuild(jda);
            if (guild == null) {
                sendEmbed(reminder, channel, embedBuilder);
                return;
            }
            guild.retrieveMemberById(reminder.userId()).queue(
                    member -> sendEmbed(reminder, channel, embedBuilder, member),
                    failed -> sendEmbed(reminder, channel, embedBuilder));
        }

        private MessageCreateAction getCreateAction(Channel channel, EmbedBuilder embedBuilder) {
            switch (channel.getType()) {
                case TEXT, NEWS, FORUM -> {
                    if (channel instanceof TextChannel textChannel) {
                        return textChannel.sendMessageEmbeds(embedBuilder.build());
                    }
                }
                case GUILD_NEWS_THREAD, GUILD_PUBLIC_THREAD, GUILD_PRIVATE_THREAD -> {
                    if (channel instanceof ThreadChannel threadChannel) {
                        return threadChannel.sendMessageEmbeds(embedBuilder.build());
                    }
                }
                default -> Logger.altitudeLogs.warning("Received unexpected channel type " + channel.getType() + " can't send reminder...");
            }
            return null;
        }

        private void sendEmbed(Reminder reminder, Channel channel, EmbedBuilder embedBuilder, Member member) {
            embedBuilder.setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl());
            switch (reminder.reminderType()) {
                case NONE, MANUAL -> {
                    MessageCreateAction createAction = getCreateAction(channel, embedBuilder);
                    if (createAction == null)
                        return;
                    createAction.queue(RestAction.getDefaultSuccess(), Util::handleFailure);
                }
                case APPEAL -> {
                    if (reminder.data() == null)
                        break;
                    InputStream inputStream = new ByteArrayInputStream(reminder.data());
                    DataInputStream dataInputStream = new DataInputStream(inputStream);
                    long userId = 0;
                    try {
                        userId = dataInputStream.readLong();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            dataInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    MessageCreateAction messageCreateAction = getCreateAction(channel, embedBuilder);
                    if (messageCreateAction == null)
                        return;
                    if (userId != 0) {
                        messageCreateAction.addContent("<@" + userId + ">");
                    }
                    messageCreateAction.queue(RestAction.getDefaultSuccess(), Util::handleFailure);
                }
            }
        }

        private void sendEmbed(Reminder reminder, Channel channel, EmbedBuilder embedBuilder) {
            embedBuilder.setAuthor(reminder.userId() + "");
            MessageCreateAction createAction = getCreateAction(channel, embedBuilder);
            if (createAction == null)
                return;
            createAction.queue(RestAction.getDefaultSuccess(), Util::handleFailure);
        }
    }
}
