package com.alttd.schedulers;

import com.alttd.AltitudeLogs;
import com.alttd.database.queries.Poll.Poll;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimerTask;

public class PollTimerTask extends TimerTask {

    private final JDA jda;
    private final AltitudeLogs logger;

    public PollTimerTask(JDA jda, AltitudeLogs logger) {
        this.jda = jda;
        this.logger = logger;
    }

    @Override
    public void run() {
        Collection<Poll> polls = Poll.getAllPolls();
        for (Poll poll : polls) {
            if (poll.needsUpdate()) {
                updatePoll(poll);
            }
        }
    }

    private void updatePoll(Poll poll) {
        Guild guild = jda.getGuildById(poll.getGuildId());
        if (guild == null) {
            logger.warning("Unable to retrieve guild for poll: " + poll.getPollId());
            return;
        }

        TextChannel textChannel = guild.getTextChannelById(poll.getChannelId());
        if (textChannel == null) {
            logger.warning("Unable to retrieve text channel for poll: " + poll.getPollId());
            return;
        }

        textChannel.retrieveMessageById(poll.getPollId()).queue(message -> updatePoll(message, poll));
    }

    private void updatePoll(Message message, Poll poll) {
        List<MessageEmbed> embeds = new ArrayList<>(message.getEmbeds());
        if (embeds.isEmpty()) {
            logger.warning("Unable to retrieve embeds for poll " + poll.getPollId());
            return;
        }

        MessageEmbed messageEmbed = embeds.get(0);
        EmbedBuilder embedBuilder = new EmbedBuilder(messageEmbed);

        embedBuilder.setFooter("Counted " + poll.getTotalVotes() + " total votes!");
        embeds.set(0, embedBuilder.build());

        message.editMessageEmbeds(embeds).queue();
        poll.setUpdated();
    }
}
