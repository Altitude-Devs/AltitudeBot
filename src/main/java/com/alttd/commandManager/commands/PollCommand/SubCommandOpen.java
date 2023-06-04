package com.alttd.commandManager.commands.PollCommand;

import com.alttd.buttonManager.ButtonManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.commandManager.SubCommandGroup;
import com.alttd.database.queries.Poll.Poll;
import com.alttd.database.queries.Poll.PollQueries;
import com.alttd.util.OptionMappingParsing;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SubCommandOpen extends SubCommand {
    private final ButtonManager buttonManager;
    protected SubCommandOpen(SubCommandGroup parentGroup, DiscordCommand parent, ButtonManager buttonManager) {
        super(parentGroup, parent);
        this.buttonManager = buttonManager;
    }

    @Override
    public String getName() {
        return "open";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        PollChannel pollChannel = PollUtil.getPollHandleErrors(event, getName());
        if (pollChannel == null)
            return;
        ReplyCallbackAction replyCallbackAction = event.deferReply(true);
        pollChannel.textChannel().retrieveMessageById(pollChannel.poll().getPollId()).queue(a -> openPoll(a, replyCallbackAction));
    }


    private void openPoll(Message message, ReplyCallbackAction replyCallbackAction) {
        boolean res = PollQueries.setPollStatus(message.getIdLong(), true, buttonManager);
        if (!res) {
            replyCallbackAction.setEmbeds(Util.genericErrorEmbed("Error", "Could not open poll in database")).queue();
            return;
        }
        List<MessageEmbed> embeds = message.getEmbeds();
        if (embeds.isEmpty()) {
            replyCallbackAction.setEmbeds(Util.genericErrorEmbed("Error", "Could not find poll embed")).queue();
            return;
        }
        MessageEmbed messageEmbed = embeds.get(0);
        EmbedBuilder embedBuilder = new EmbedBuilder(messageEmbed);
        embedBuilder.setColor(Color.GREEN);
        message.editMessageEmbeds(embedBuilder.build()).queue(success -> replyCallbackAction.setEmbeds(Util.genericSuccessEmbed("Success", "Poll opened")).queue());
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
