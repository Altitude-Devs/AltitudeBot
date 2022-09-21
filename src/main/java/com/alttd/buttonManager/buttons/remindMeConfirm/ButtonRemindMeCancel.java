package com.alttd.buttonManager.buttons.remindMeConfirm;

import com.alttd.buttonManager.DiscordButton;
import com.alttd.util.Util;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class ButtonRemindMeCancel extends DiscordButton {
    @Override
    public String getButtonId() {
        return "remind_me_cancel";
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        ButtonRemindMeConfirm.removeReminder(event.getUser().getIdLong());
        event.replyEmbeds(Util.genericSuccessEmbed("Success", "Cancelled your reminder!"))
                .setEphemeral(true).queue();
    }

    @Override
    public Button getButton() {
        return Button.danger(getButtonId(), "Cancel");
    }
}
