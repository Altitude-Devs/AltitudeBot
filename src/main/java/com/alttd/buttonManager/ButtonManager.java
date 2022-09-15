package com.alttd.buttonManager;

import com.alttd.buttonManager.buttons.suggestionReview.ButtonSuggestionReviewAccept;
import com.alttd.buttonManager.buttons.suggestionReview.ButtonSuggestionReviewDeny;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ButtonManager extends ListenerAdapter {

    private final List<DiscordButton> buttons;

    public ButtonManager() {
        buttons = List.of(
                new ButtonSuggestionReviewAccept(),
                new ButtonSuggestionReviewDeny());
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {

    }

    public @Nullable Button getButtonFor(String buttonId) {
        Optional<DiscordButton> first = buttons.stream()
                .filter(discordButton -> discordButton.getButtonId().equalsIgnoreCase(buttonId))
                .findFirst();
        if (first.isEmpty())
            return null;
        return first.get().getButton();
    }

}
