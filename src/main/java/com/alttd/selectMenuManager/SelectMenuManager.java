package com.alttd.selectMenuManager;

import com.alttd.selectMenuManager.selectMenus.SelectMenuAuction;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class SelectMenuManager extends ListenerAdapter {

    private final List<DiscordSelectMenu> buttons;

    public SelectMenuManager() {
        buttons = List.of(new SelectMenuAuction(this));
    }

    @Override
    public void onGenericSelectMenuInteraction(@NotNull GenericSelectMenuInteractionEvent event) {
        String selectMenuId = event.getSelectMenu().getId();
        Optional<DiscordSelectMenu> first = buttons.stream()
                .filter(discordModal -> discordModal.getSelectMenuId().equalsIgnoreCase(selectMenuId))
                .findFirst();
        if (first.isEmpty()) {
//            event.replyEmbeds(new EmbedBuilder()
//                            .setTitle("Invalid command")
//                            .setDescription("Unable to process select menu with id: [" + selectMenuId + "], please report this issue to a Teri")
//                            .setColor(Color.RED)
//                            .build())
//                    .setEphemeral(true)
//                    .queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        if (event instanceof StringSelectInteractionEvent stringSelectInteractionEvent)
            first.get().execute(stringSelectInteractionEvent);
    }

    public @Nullable DiscordSelectMenu getDiscordSelectMenuFor(String buttonId) {
        Optional<DiscordSelectMenu> first = buttons.stream()
                .filter(discordSelectMenu -> discordSelectMenu.getSelectMenuId().equalsIgnoreCase(buttonId))
                .findFirst();
        return first.orElse(null);
    }

}
