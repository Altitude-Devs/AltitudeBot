package com.alttd.buttonManager.buttons.autoReminder;

import com.alttd.buttonManager.DiscordButton;
import com.alttd.schedulers.ReminderScheduler;
import com.alttd.util.Logger;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.Collections;
import java.util.Objects;

public class ButtonRejected extends DiscordButton {
    @Override
    public String getButtonId() {
        return "reminder_denied";
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        Message message = event.getMessage();
        if (!ButtonReminderUtil.shouldExecute(message, event))
            return;
        Logger.altitudeLogs.debug("Rejecting reminder");
        MessageEmbed embed = message.getEmbeds().get(0);
        EmbedBuilder embedBuilder = new EmbedBuilder(embed).setColor(Color.RED);
        ReminderScheduler.getInstance(event.getJDA()).removeReminder(message.getIdLong());
        message.editMessageEmbeds(embedBuilder.build()).queue();
        message.editMessageComponents().setComponents(Collections.emptyList()).queue();
        event.replyEmbeds(Util.genericSuccessEmbed("Success", "This message has been marked as Denied"))
                .setEphemeral(true).queue();
        ThreadChannel startedThread = message.getStartedThread();
        Member member = event.getMember();
        if (startedThread != null && member != null)
            startedThread.sendMessage("Marked as rejected by " + member.getAsMention() + " at <t:" + System.currentTimeMillis() / 1000 + ":F>").queue();
    }

    @Override
    public Button getButton() {
        return Button.danger(getButtonId(), "Denied");
    }
}
