package com.alttd.buttonManager.buttons.remindMeConfirm;

import com.alttd.buttonManager.DiscordButton;
import com.alttd.database.queries.QueriesReminders.Reminder;
import com.alttd.util.Util;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ButtonRemindMeCancel extends DiscordButton {
    @Override
    public String getButtonId() {
        return "remind_me_cancel";
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        HookAndReminder hookAndReminder = ButtonRemindMeConfirm.removeReminder(event.getUser().getIdLong());
        if (hookAndReminder == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Your reminder was already cancelled!"))
                    .setEphemeral(true).queue();
            return;
        }
        event.replyEmbeds(Util.genericSuccessEmbed("Success", "Cancelled your reminder!"))
                .setEphemeral(true).queue();
        hookAndReminder.interactionHook().editOriginalComponents(List.of()).queue();
    }

    @Override
    public Button getButton() {
        return Button.danger(getButtonId(), "Cancel");
    }
}
