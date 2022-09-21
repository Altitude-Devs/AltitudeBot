package com.alttd.reminders;

import com.alttd.database.queries.QueriesReminders.QueriesReminders;
import com.alttd.database.queries.QueriesReminders.Reminder;
import com.alttd.util.Logger;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
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
            Logger.severe("Unable to retrieve reminders");
            instance = null;
            return;
        }
        reminders.sort(Comparator.comparingLong(Reminder::remindDate));
        if (reminders.size() == 0)
            nextReminder = null;
        else
            nextReminder = reminders.get(0);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.schedule(new ReminderRun(), 1, TimeUnit.MINUTES);

    }

    public static ReminderScheduler getInstance(JDA jda) {
        if (instance == null)
            instance = new ReminderScheduler(jda);
        return instance;
    }

    public synchronized void addReminder(Reminder reminder) {
        reminders.add(reminder);
        reminders.sort(Comparator.comparingLong(Reminder::remindDate));
        nextReminder = reminders.get(0);
    }

    public synchronized void removeReminder(Reminder reminder) {
        reminders.remove(reminder);
        if (reminders.size() == 0)
            nextReminder = null;
        else
            nextReminder = reminders.get(0);
        QueriesReminders.removeReminder(reminder.id());
    }

    private class ReminderRun implements Runnable {

        @Override
        public void run() {
            long time = new Date().getTime();
            while (nextReminder != null && time > nextReminder.remindDate()) {
                //TODO run reminder
                TextChannel channel = nextReminder.getChannel(jda);
                if (channel == null || !channel.canTalk()) {
                    Logger.warning("Unable to run reminder: " + nextReminder.id() +
                            "\ntitle: [" + nextReminder.title() +
                            "]\ndescription: [" + nextReminder.description() + "]");
                    return;
                }
                sendEmbed(nextReminder, channel);
                removeReminder(nextReminder);
            }
        }

        private void sendEmbed(Reminder reminder, TextChannel channel) {
            long discordTimestamp = TimeUtil.getDiscordTimestamp(reminder.creationDate());
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle(reminder.title())
                    .setDescription(reminder.description())
                    .setFooter("Requested <t:" + discordTimestamp + ":R>");
            Guild guild = reminder.getGuild(jda);
            if (guild == null) {
                sendEmbed(reminder, channel, embedBuilder);
                return;
            }
            guild.retrieveMemberById(reminder.userId()).queue(
                    member -> sendEmbed(reminder, channel, embedBuilder, member),
                    failed -> sendEmbed(reminder, channel, embedBuilder));
        }

        private void sendEmbed(Reminder reminder, TextChannel channel, EmbedBuilder embedBuilder, Member member) {
            embedBuilder.setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl());
            channel.sendMessageEmbeds(embedBuilder.build()).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
        }

        private void sendEmbed(Reminder reminder, TextChannel channel, EmbedBuilder embedBuilder) {
            embedBuilder.setAuthor(reminder.userId() + "");
            channel.sendMessageEmbeds(embedBuilder.build()).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
        }
    }
}
