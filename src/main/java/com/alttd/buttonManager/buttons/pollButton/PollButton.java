package com.alttd.buttonManager.buttons.pollButton;

import com.alttd.buttonManager.DiscordButton;
import com.alttd.database.queries.Poll.Poll;
import com.alttd.database.queries.Poll.PollButtonClicksQueries;
import com.alttd.util.Util;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.HashSet;

public class PollButton extends DiscordButton {

    private final long internalId;
    private final String buttonId;
    private final String buttonTitle;
    private final HashSet<Long> userClicks;

    public PollButton(long internalId, String buttonId, String buttonTitle, HashSet<Long> userClicks) {
        this.internalId = internalId;
        this.buttonId = buttonId;
        this.buttonTitle = buttonTitle;
        this.userClicks = userClicks;
    }

    @Override
    public String getButtonId() {
        return buttonId;
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        long userId = event.getUser().getIdLong();
        Poll poll = Poll.getPoll(event.getMessage().getIdLong());
        if (!poll.isActive()) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to process your vote for " + buttonTitle + ", the poll might have ended!")).setEphemeral(true).queue();
            return;
        }
        if (userClicks.contains(userId)) {
            PollButtonClicksQueries.removeButtonClick(poll.getPollId(), userId);
            userClicks.remove(userId);
            poll.receivedVote();
            event.replyEmbeds(Util.genericSuccessEmbed("Success", "Removed your vote from " + buttonTitle + ", thanks!")).setEphemeral(true).queue();
            return;
        }
        poll.resetClicks(userId);
        PollButtonClicksQueries.addButtonClick(poll.getPollId(), internalId, userId);
        userClicks.add(userId);
        poll.receivedVote();
        event.replyEmbeds(Util.genericSuccessEmbed("Success", "You voted on " + buttonTitle + ", thanks!")).setEphemeral(true).queue();
    }

    @Override
    public Button getButton() {
        return Button.primary(getButtonId(), buttonTitle);
    }

    public long getInternalId() {
        return internalId;
    }

    public void removeClick(long userId) {
        userClicks.remove(userId);
    }

    public String getButtonTitle() {
        return buttonTitle;
    }

    public int getVotes() {
        return userClicks.size();
    }
}
