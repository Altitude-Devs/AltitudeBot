package com.alttd.commandManager.commands.PollCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ButtonData { //TODO add a feature that updates a total votes count on polls while they're active
    private final UUID buttonId;
    private final long pollId;
    List<Long> votes;

    public ButtonData(UUID buttonId, long pollId) {
        this.buttonId = buttonId;
        this.pollId = pollId;
        votes = new ArrayList<>();
    }

    public ButtonData(UUID buttonId, long pollId, List<Long> votes) {
        this.buttonId = buttonId;
        this.pollId = pollId;
        this.votes = votes;
    }

    public UUID getButtonId() {
        return buttonId;
    }

    public long getPollId() {
        return pollId;
    }

    public int totalVotes() {
        return votes.size();
    }

    public boolean addVote(long id) {
        if (votes.contains(id))
            return false;
        votes.add(id);
        return true;
    }

    public boolean removeVote(long id) {
        if (!votes.contains(id))
            return false;
        votes.remove(id);
        return true;
    }
}
