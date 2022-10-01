package com.alttd.modalManager;

import com.alttd.buttonManager.ButtonManager;
import com.alttd.modalManager.modals.*;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class ModalManager extends ListenerAdapter {

    private final List<DiscordModal> modals;

    public ModalManager(ButtonManager buttonManager) {
        modals = List.of(
                new ModalSuggestion(buttonManager),
                new ModalEvidence(),
                new ModalReplySuggestion(),
                new ModalRemindMe(buttonManager),
                new ModalCrateItem());
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String modalId = event.getModalId();
        Optional<DiscordModal> first = modals.stream()
                .filter(discordModal -> discordModal.getModalId().equalsIgnoreCase(modalId))
                .findFirst();
        if (first.isEmpty()) {
            event.replyEmbeds(new EmbedBuilder()
                            .setTitle("Invalid command")
                            .setDescription("Unable to process modal with id: [" + modalId + "], please report this issue to a Teri")
                            .setColor(Color.RED)
                            .build())
                    .setEphemeral(true)
                    .queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        first.get().execute(event);
    }

    public @Nullable Modal getModalFor(String modalId) {
        Optional<DiscordModal> first = modals.stream()
                .filter(discordModal -> discordModal.getModalId().equalsIgnoreCase(modalId))
                .findFirst();
        if (first.isEmpty())
            return null;
        return first.get().getModal();
    }
}
