package com.alttd.buttonManager.buttons.autoReminder;

import com.alttd.buttonManager.DiscordButton;
import com.alttd.schedulers.ReminderScheduler;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.Collections;

public class ButtonAccepted extends DiscordButton {
    @Override
    public String getButtonId() {
        return "reminder_accepted";
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        Message message = event.getMessage();
        if (!ButtonReminderUtil.shouldExecute(message, event))
            return;
        MessageEmbed embed = message.getEmbeds().get(0);
        EmbedBuilder embedBuilder = new EmbedBuilder(embed).setColor(Color.GREEN);
        ReminderScheduler.getInstance(event.getJDA()).removeReminder(message.getIdLong());
        message.editMessageEmbeds(embedBuilder.build()).queue();
        message.editMessageComponents().setComponents(Collections.emptyList()).queue();
        event.replyEmbeds(Util.genericSuccessEmbed("Success", "This message has been marked as Accepted"))
                .setEphemeral(true).queue();
    }

    @Override
    public Button getButton() {
        return Button.primary(getButtonId(), "Accepted");
    }
}
