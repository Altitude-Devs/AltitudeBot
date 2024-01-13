package com.alttd.modalManager.modals;

import com.alttd.modalManager.DiscordModal;
import com.alttd.util.Util;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.HashMap;

public class ModalReplySuggestion extends DiscordModal {

    private static final HashMap<Long, Message> userToMessageMap = new HashMap<>();

    public static synchronized void putMessage(long userId, Message message) {
        userToMessageMap.put(userId, message);
    }

    private static synchronized Message pullMessage(long userId) {
        return userToMessageMap.remove(userId);
    }

    @Override
    public String getModalId() {
        return "reply_suggestion";
    }

    @Override
    public void execute(ModalInteractionEvent event) {
        ModalMapping modalMapping = event.getInteraction().getValue("response");
        if (modalMapping == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to find response in modal"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        String response = modalMapping.getAsString();
        if (response.isEmpty()) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Response in modal is empty"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        Member member = event.getMember();
        if (member == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This modal only works from within a guild"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        Message message = pullMessage(member.getIdLong());
        if (message == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to find a message for this modal"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        String[] split = message.getContentRaw().split("\u200B");
        if (split.length == 0) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "The suggestion to be edited has no content"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        message.editMessage(split[0] + "\u200B\n\n" + "**Response by: " + member.getAsMention() + "**\n_" + response.replaceAll("\u200B", "") + "_")
                .queue(success -> event.replyEmbeds(Util.genericSuccessEmbed("Success", "Responded to the suggestion!"))
                                .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure),
                        failure -> event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to edit the suggestion"))
                                .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure));
    }

    @Override
    public Modal getModal() {
        TextInput body = TextInput.create("response", "Response", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Response...")
                .setRequiredRange(10, 1024)
                .setRequired(true)
                .build();

        return Modal.create(getModalId(), "Suggestion Response")
                .addActionRows(ActionRow.of(body))
                .build();
    }
}
