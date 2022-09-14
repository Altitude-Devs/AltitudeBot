package com.alttd.modalManager.modals;

import com.alttd.modalManager.DiscordModal;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class ModalSuggestion extends DiscordModal {
    @Override
    public String getModalId() {
        return "suggestion";
    }

    @Override
    public void execute(ModalInteractionEvent event) {

    }

    @Override
    public Modal getModal() {
        TextInput title = TextInput.create("title", "Title", TextInputStyle.SHORT)
                .setPlaceholder("You suggestion in one sentence")
                .setRequiredRange(10, 100)
                .setRequired(true)
                .build();

        TextInput body = TextInput.create("body", "Body", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Your concerns go here")
                .setRequiredRange(30, 1024)
                .setRequired(true)
                .build();

        return Modal.create(getModalId(), "Suggestion Form")
                .addActionRows(ActionRow.of(title), ActionRow.of(body))
                .build();
    }
}
