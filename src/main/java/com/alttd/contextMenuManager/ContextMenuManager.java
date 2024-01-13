package com.alttd.contextMenuManager;

import com.alttd.contextMenuManager.contextMenus.ContextMenuRespondSuggestion;
import com.alttd.modalManager.ModalManager;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NonNls;


import java.awt.*;
import java.util.List;
import java.util.Optional;

public class ContextMenuManager extends ListenerAdapter {

    private final List<DiscordContextMenu> contextMenus;

    public ContextMenuManager(ModalManager modalManager) {
        contextMenus = List.of(
                new ContextMenuRespondSuggestion(modalManager)
        );
    }

    public DiscordContextMenu getContext(String name) {
        for (DiscordContextMenu contextMenu : contextMenus) {
            if (contextMenu.getContextMenuId().equalsIgnoreCase(name))
                return contextMenu;
        }
        return null;
    }

    public List<DiscordContextMenu> getContexts() {
        return contextMenus;
    }

    @Override
    public void onUserContextInteraction(@NonNls UserContextInteractionEvent event) {
        String name = event.getInteraction().getName();
        Optional<DiscordContextMenu> first = contextMenus.stream()
                .filter(discordModal -> discordModal.getContextMenuId().equalsIgnoreCase(name))
                .findFirst();
        if (first.isEmpty()) {
            event.replyEmbeds(new EmbedBuilder()
                            .setTitle("Invalid command")
                            .setDescription("Unable to process user context interaction with id: [" + name + "].")
                            .setColor(Color.RED)
                            .build())
                    .setEphemeral(true)
                    .queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        first.get().execute(event);
    }

    @Override
    public void onMessageContextInteraction(@NonNls MessageContextInteractionEvent event) {
        String name = event.getInteraction().getName();
        Optional<DiscordContextMenu> first = contextMenus.stream()
                .filter(discordModal -> discordModal.getContextMenuId().equalsIgnoreCase(name))
                .findFirst();
        if (first.isEmpty()) {
            event.replyEmbeds(new EmbedBuilder()
                            .setTitle("Invalid command")
                            .setDescription("Unable to process user context interaction with id: [" + name + "].")
                            .setColor(Color.RED)
                            .build())
                    .setEphemeral(true)
                    .queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        first.get().execute(event);
    }
}
