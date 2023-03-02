package com.alttd.listeners;

import com.alttd.AltitudeBot;
import com.alttd.buttonManager.ButtonManager;
import com.alttd.database.queries.QueriesAssignAppeal;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import javax.annotation.Nonnull;
import java.util.List;

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
        message.getChannel().sendMessageEmbeds(embed).queue(res -> {
                res.editMessageComponents().setActionRow(reminderAccepted, reminderInProgress, reminderDenied).queue();
                res.createThreadChannel("Appeal").queue();
        });
        message.delete().queue();
    }

}
