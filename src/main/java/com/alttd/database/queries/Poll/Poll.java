package com.alttd.database.queries.Poll;

import com.alttd.buttonManager.buttons.pollButton.PollButton;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Poll {

    private static final HashMap<Long, Poll> polls = new HashMap<>();
    public static Poll getPoll(long pollId) {
        return polls.get(pollId);
    }

    public static List<Poll> getGuildPolls(long guildId) {
        return polls.values().stream().filter(poll -> poll.getGuildId() == guildId).collect(Collectors.toList());
    }

    public static Collection<Poll> getAllPolls() {
        return polls.values();
    }

    private static void addPoll(Poll poll) {
        polls.put(poll.getPollId(), poll);
    }

    private final long pollId;
    private final long channelId;
    private final long guildId;
    private boolean active;
    private String title;
    private final List<PollButton> buttons = new ArrayList<>();
    private boolean needsUpdate = false;


    public Poll(long pollId, long channelId, long guildId, boolean active, String title) {
        this.pollId = pollId;
        this.channelId = channelId;
        this.guildId = guildId;
        this.active = active;
        this.title = title;
        Poll.addPoll(this);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void resetClicks(long userId) {
        for (PollButton button : buttons) {
            button.removeClick(userId);
        }
    }

    public void addButton(PollButton button) {
        if (buttons.stream()
                .filter(listButton -> listButton.getButtonId().equals(button.getButtonId()))
                .findAny()
                .isEmpty())
            buttons.add(button);
    }

    public void addButtons(List<PollButton> buttons) {
        buttons.forEach(this::addButton);
    }

    public void removeButton(PollButton button) {
        buttons.remove(button);
    }

    public long getPollId() {
        return pollId;
    }

    public long getChannelId() {
        return channelId;
    }

    public long getGuildId() {
        return guildId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    public String getTitle() {
        return title;
    }

    public PollButton getButton(long internalId) {
        return buttons.stream().filter(button -> button.getInternalId() == internalId).findAny().orElse(null);
    }

    public MessageEmbed getVotesEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder
                .setColor(Color.GREEN)
                .setTitle(getEmbedTitle());
        buttons.sort(Comparator.comparingLong(PollButton::getInternalId));
        for (PollButton button : buttons) {
            embedBuilder.addField(button.getButtonTitle(), String.valueOf(button.getVotes()), true);
        }

        return embedBuilder.build();
    }

    private String getEmbedTitle() {
        String embedTitle = "Results for: " + title;
        if (embedTitle.length() > MessageEmbed.TITLE_MAX_LENGTH)
            embedTitle = embedTitle.substring(0, MessageEmbed.TITLE_MAX_LENGTH - 3) + "...";
        return embedTitle;
    }

    public boolean needsUpdate() {
        return needsUpdate;
    }

    public void receivedVote() {
        needsUpdate = true;
    }

    public void setUpdated() {
        needsUpdate = false;
    }

    public long getTotalVotes() {
        return buttons.stream().map(PollButton::getVotes).mapToInt(Integer::intValue).sum();
    }
}
