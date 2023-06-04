package com.alttd.buttonManager;

import com.alttd.buttonManager.buttons.autoReminder.ButtonAccepted;
import com.alttd.buttonManager.buttons.autoReminder.ButtonInProgress;
import com.alttd.buttonManager.buttons.autoReminder.ButtonRejected;
import com.alttd.buttonManager.buttons.remindMeConfirm.ButtonRemindMeCancel;
import com.alttd.buttonManager.buttons.remindMeConfirm.ButtonRemindMeConfirm;
import com.alttd.buttonManager.buttons.suggestionReview.ButtonSuggestionReviewAccept;
import com.alttd.buttonManager.buttons.suggestionReview.ButtonSuggestionReviewDeny;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ButtonManager extends ListenerAdapter {

    private final List<DiscordButton> buttons;

    public ButtonManager() {
        buttons = new ArrayList<>();
        buttons.add(new ButtonSuggestionReviewAccept());
        buttons.add(new ButtonSuggestionReviewDeny());
        buttons.add(new ButtonRemindMeCancel());
        buttons.add(new ButtonRemindMeConfirm());
        buttons.add(new ButtonAccepted());
        buttons.add(new ButtonInProgress());
        buttons.add(new ButtonRejected());
    }

    public void addButton(DiscordButton button) {
        buttons.add(button);
    }

    public void removeButton(DiscordButton button) {

    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        Optional<DiscordButton> first = buttons.stream()
                .filter(discordModal -> discordModal.getButtonId().equalsIgnoreCase(buttonId))
                .findFirst();
        if (first.isEmpty()) {
//            event.replyEmbeds(new EmbedBuilder()
//                            .setTitle("Invalid command")
//                            .setDescription("Unable to process button with id: [" + buttonId + "], please report this issue to a Teri")
//                            .setColor(Color.RED)
//                            .build())
//                    .setEphemeral(true)
//                    .queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        first.get().execute(event);
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
