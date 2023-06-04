package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.commandManager.SubCommandGroup;
import com.alttd.database.queries.Poll.Poll;
import com.alttd.util.OptionMappingParsing;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SubCommandEditTitle extends SubCommand {
    protected SubCommandEditTitle(SubCommandGroup parentGroup, DiscordCommand parent) {
        super(parentGroup, parent);
    }

    @Override
    public String getName() {
        return "edit_title";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        PollChannel pollChannel = PollUtil.getPollHandleErrors(event, getName());
        if (pollChannel == null)
            return;

        String title = OptionMappingParsing.getString("title", event, getName());
        if (title == null || title.length() > 256) {
            if (title == null)
                event.replyEmbeds(Util.genericErrorEmbed("Error", "No title found")).setEphemeral(true).queue();
            else
                event.replyEmbeds(Util.genericErrorEmbed("Error", "Title too long")).setEphemeral(true).queue();
            return;
        }

        event.replyEmbeds(Util.genericWaitingEmbed("Waiting...", "Editing poll...")).setEphemeral(true).queue(hook -> {
            pollChannel.textChannel().retrieveMessageById(pollChannel.poll().getPollId()).queue(message -> updatePoll(message, title, hook),
                    error -> hook.editOriginalEmbeds(Util.genericErrorEmbed("Error", "Unable to find message with id [" + pollChannel.poll().getPollId() + "].")).queue());
        });
    }

    private void updatePoll(Message message, String title, InteractionHook hook) {
        EmbedBuilder firstEmbedBuilder = Util.getFirstEmbedBuilder(message);
        if (firstEmbedBuilder == null) {
            hook.editOriginalEmbeds(Util.genericErrorEmbed("Error", "Unable to get embed from poll message."))
                    .queue();
            return;
        }
        message.editMessageEmbeds(firstEmbedBuilder.setTitle(title).build()).queue(
                resultMessage -> hook.editOriginalEmbeds(Util.genericSuccessEmbed("Success", "Updated the poll title.")).queue(),
                error -> hook.editOriginalEmbeds(Util.genericErrorEmbed("Error", "Unable to edit poll message.")).queue());
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
