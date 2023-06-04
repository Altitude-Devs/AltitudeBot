package com.alttd.commandManager.commands.PollCommand;

import com.alttd.buttonManager.ButtonManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.commandManager.SubCommandGroup;
import com.alttd.database.queries.Poll.Poll;
import com.alttd.database.queries.Poll.PollQueries;
import com.alttd.util.Util;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.List;

public class SubCommandClose extends SubCommand {

    private final ButtonManager buttonManager;
    protected SubCommandClose(SubCommandGroup parentGroup, DiscordCommand parent, ButtonManager buttonManager) {
        super(parentGroup, parent);
        this.buttonManager = buttonManager;
    }

    @Override
    public String getName() {
        return "close";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        PollChannel pollChannel = PollUtil.getPollHandleErrors(event, getName());
        if (pollChannel == null)
            return;
        Poll poll = pollChannel.poll();
        ReplyCallbackAction replyCallbackAction = event.deferReply(true);
        pollChannel.textChannel().retrieveMessageById(poll.getPollId()).queue(message -> closePoll(message, poll, replyCallbackAction));
    }

    private void closePoll(Message message, Poll poll, ReplyCallbackAction replyCallbackAction) {
        List<MessageEmbed> embeds = message.getEmbeds();
        if (embeds.size() > 1) {
            replyCallbackAction.setEmbeds(Util.genericErrorEmbed("Error", "This poll has already been closed!")).queue();
            return;
        }
        message.editMessageEmbeds(embeds.get(0), poll.getVotesEmbed()).queue();
        PollQueries.setPollStatus(poll.getPollId(), false, buttonManager);
        replyCallbackAction.setEmbeds(Util.genericSuccessEmbed("Success", "This poll has been closed!")).queue();
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        PollUtil.handleSuggestMessageId(event);
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
