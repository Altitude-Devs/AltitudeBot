package com.alttd.listeners;

import com.alttd.buttonManager.ButtonManager;
import com.alttd.database.queries.QueriesAssignAppeal;
import com.alttd.database.queries.QueriesReminders.QueriesReminders;
import com.alttd.database.queries.QueriesReminders.Reminder;
import com.alttd.database.queries.QueriesReminders.ReminderType;
import com.alttd.schedulers.ReminderScheduler;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AppealRepost extends ListenerAdapter {

    private final ButtonManager buttonManager;

    public AppealRepost(ButtonManager buttonManager) {
        Logger.info("Created Appeal Repost -----------------------------------------------------------------------------------------------------------");
        this.buttonManager = buttonManager;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        Logger.info("Message received:");
        Logger.info(event.getMessage() + "\n\n" + event.getMessage().getContentRaw() + "\n\nembeds: " + event.getMessage().getEmbeds().size());
        if (event.getMember() != null) { //Webhooks aren't members
            Logger.info("Return 1");
            return;
        }
        if (event.getGuild().getIdLong() != 514920774923059209L) {
            Logger.info("Return 2");
            return;
        }
        if (event.getChannel().getIdLong() != 514922555950235681L) {
            Logger.info("Return 3 channel was: " + event.getChannel().getId());
            return;
        }
        Message message = event.getMessage();
        List<MessageEmbed> embeds = message.getEmbeds();
        if (embeds.size() == 0) {
            Logger.info("Return 4");
            return;
        }
        MessageEmbed messageEmbed = embeds.get(0);
        List<MessageEmbed.Field> fields = messageEmbed.getFields();
        if (fields.size() == 0) {
            Logger.info("Return 5");
            return;
        }
        String name = fields.get(0).getName();
        if (name == null || !name.equals("Punishment info")) {
            Logger.info("Return 6");
            return;
        }
        EmbedBuilder embedBuilder = new EmbedBuilder(messageEmbed);
        long userId = QueriesAssignAppeal.getAssignAppeal();
        if (userId == -1){
            Logger.info("user id was -1");
            assignAndSendAppeal(embedBuilder, message, null);
        } else {
            Guild guild = message.getGuild();
            Member member = guild.getMemberById(userId);
            if (member != null) {
                Logger.info("member was in cache");
                assignAndSendAppeal(embedBuilder, message, member);
            } else {
                Logger.info("member wasn't in cache");
                guild.retrieveMemberById(userId).queue(result -> assignAndSendAppeal(embedBuilder, message, result));
            }
        }
    }

    private void assignAndSendAppeal(EmbedBuilder embedBuilder, Message message, Member member) {
        embedBuilder.addField(
                "Assigned to " + (member == null ? " no one since we couldn't find a next user (check console)" : member.getEffectiveName()),
                "",
                false);
        MessageEmbed embed = embedBuilder.build();
        Button reminderAccepted = buttonManager.getButtonFor("reminder_accepted");
        Button reminderInProgress = buttonManager.getButtonFor("reminder_in_progress");
        Button reminderDenied = buttonManager.getButtonFor("reminder_denied");
        if (reminderAccepted == null || reminderInProgress == null || reminderDenied == null) {
            Logger.warning("Unable to get a button for appeals");
            return;
        }
        MessageCreateAction messageCreateAction = message.getChannel().sendMessageEmbeds(embed);
        if (member != null)
            messageCreateAction = messageCreateAction.mentionUsers(member.getIdLong());
        messageCreateAction.queue(res -> {
                res.editMessageComponents().setActionRow(reminderAccepted, reminderInProgress, reminderDenied).queue();
                res.createThreadChannel("Appeal").queue((
                        threadChannel -> scheduleReminder(res, member, threadChannel)),
                        failure -> Logger.warning("Unable to create thread channel so won't schedule reminder..."));
        });
        message.delete().queue();

    }

    private void scheduleReminder(Message message, @Nullable Member member, ThreadChannel threadChannel) {
        String memberMention = member == null ? "the appeals team" : member.getAsMention();
        ByteArrayOutputStream outputStream = null;
        if (member != null) {
            outputStream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(outputStream);
            try {
                out.writeLong(member.getIdLong());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Reminder reminder = new Reminder(
                -1,
                "Reminder",
                "I am reminding " + memberMention + " that we aim to resolve appeals within 24h or in more complex circumstances 48 hours!",
                member == null ? 0 : member.getIdLong(),
                message.getGuild().getIdLong(),
                threadChannel.getIdLong(),
                message.getIdLong(),
                true,
                System.currentTimeMillis(),
                System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1),
                ReminderType.APPEAL,
                outputStream == null ? null : outputStream.toByteArray()
        );

        int id = QueriesReminders.storeReminder(reminder);
        if (id == 0) {
            Logger.warning("Unable to store reminder for appeal with message id: " + message.getId());
            return;
        }

        reminder = Reminder.reCreateReminder(id, reminder);

        ReminderScheduler instance = ReminderScheduler.getInstance(message.getJDA());
        if (instance == null) {
            QueriesReminders.removeReminder(reminder.id());
            Logger.warning("Unable to start reminder, removing it from the database...");
            return;
        }

        instance.addReminder(reminder);
    }

}
