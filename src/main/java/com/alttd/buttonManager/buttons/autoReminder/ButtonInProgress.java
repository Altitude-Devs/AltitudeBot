package com.alttd.buttonManager.buttons.autoReminder;

import com.alttd.buttonManager.DiscordButton;
import com.alttd.util.Logger;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

public class ButtonInProgress extends DiscordButton {
    @Override
    public String getButtonId() {
        return "reminder_in_progress";
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        Message message = event.getMessage();
        if (!ButtonReminderUtil.shouldExecute(message, event))
            return;
        Logger.altitudeLogs.debug("Marking reminder as in progress");
        MessageEmbed embed = message.getEmbeds().get(0);
        EmbedBuilder embedBuilder = new EmbedBuilder(embed);
        Color color = embed.getColor();
        if (color == null || !color.equals(Color.ORANGE)) {
            embedBuilder.setColor(Color.ORANGE).build();
        } else {
            embedBuilder.setColor(Color.GRAY).build();
        }
        message.editMessageEmbeds(embedBuilder.build()).queue();
        event.replyEmbeds(Util.genericSuccessEmbed("Success", "This message has been marked as In Progress"))
                .setEphemeral(true).queue();
    }

    @Override
    public Button getButton() {
        return Button.secondary(getButtonId(), "In Progress");
    }
}
