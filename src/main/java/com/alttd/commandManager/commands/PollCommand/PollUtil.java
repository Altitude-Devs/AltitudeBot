package com.alttd.commandManager.commands.PollCommand;

import com.alttd.AltitudeLogs;
import com.alttd.database.queries.Poll.Poll;
import com.alttd.util.OptionMappingParsing;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PollUtil {

    protected static PollChannel getPollHandleErrors(SlashCommandInteractionEvent event, String name) {
        Long messageId = Util.parseLong(OptionMappingParsing.getString("message_id", event, name));
        if (messageId == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Invalid message id")).setEphemeral(true).queue();
            return null;
        }

        Poll poll = Poll.getPoll(messageId);
        if (poll == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "There is no poll with this message id")).setEphemeral(true).queue();
            return null;
        }

        Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "There is no guild for this event")).setEphemeral(true).queue();
            return null;
        }

        TextChannel textChannel = guild.getTextChannelById(poll.getChannelId());
        if (textChannel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Cannot find the poll channel")).setEphemeral(true).queue();
            return null;
        }

        if (!textChannel.canTalk()) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "I cannot talk in this channel")).setEphemeral(true).queue();
            return null;
        }

        Member member = event.getMember();
        if (member == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Cannot find you as a member")).setEphemeral(true).queue();
            return null;
        }

        if (!textChannel.canTalk(member)) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "You cannot talk in this channel")).setEphemeral(true).queue();
            return null;
        }
        return new PollChannel(poll, textChannel);
    }

    protected static void handleSuggestMessageId(CommandAutoCompleteInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.replyChoices(new ArrayList<>()).queue();
            return;
        }
        AutoCompleteQuery focusedOption = event.getFocusedOption();
        if (focusedOption.getName().equals("message_id")) {
            List<Poll> guildPolls = Poll.getGuildPolls(event.getGuild().getIdLong());
            try {
                String value = focusedOption.getValue();
                event.replyChoiceLongs(guildPolls.stream().map(Poll::getPollId).filter(pollId -> String.valueOf(pollId).startsWith(value)).collect(Collectors.toList())).queue();
            } catch (NumberFormatException ignored) {
                event.replyChoices(new ArrayList<>()).queue();
            }
        }
    }

    public static void updatePoll(JDA jda, AltitudeLogs logger, Poll poll) {
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

        textChannel.retrieveMessageById(poll.getPollId()).queue(message -> updatePoll(logger, message, poll));
    }

    private static void updatePoll(AltitudeLogs logger, Message message, Poll poll) {
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

