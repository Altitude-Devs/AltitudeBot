package com.alttd.selectMenuManager;

import com.alttd.selectMenuManager.selectMenus.SelectMenuAuction;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class SelectMenuManager extends ListenerAdapter {

    private final List<DiscordSelectMenu> buttons;

    public SelectMenuManager() {
        buttons = List.of(new SelectMenuAuction());
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        String selectMenuId = event.getSelectMenu().getId();
        Optional<DiscordSelectMenu> first = buttons.stream()
                .filter(discordModal -> discordModal.getSelectMenuId().equalsIgnoreCase(selectMenuId))
                .findFirst();
        if (first.isEmpty()) {
            event.replyEmbeds(new EmbedBuilder()
                            .setTitle("Invalid command")
                            .setDescription("Unable to process select menu with id: [" + selectMenuId + "], please report this issue to a Teri")
                            .setColor(Color.RED)
                            .build())
                    .setEphemeral(true)
                    .queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        first.get().execute(event);
    }

    public @Nullable DiscordSelectMenu getDiscordSelectMenuFor(String buttonId) {
        Optional<DiscordSelectMenu> first = buttons.stream()
                .filter(discordSelectMenu -> discordSelectMenu.getSelectMenuId().equalsIgnoreCase(buttonId))
                .findFirst();
        if (first.isEmpty())
            return null;
        return first.get();
    }

}
