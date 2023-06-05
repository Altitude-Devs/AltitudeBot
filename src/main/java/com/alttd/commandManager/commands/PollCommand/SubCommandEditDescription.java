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

public class SubCommandEditDescription extends SubCommand {
    protected SubCommandEditDescription(SubCommandGroup parentGroup, DiscordCommand parent) {
        super(parentGroup, parent);
    }

    @Override
    public String getName() {
        return "edit_description";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        PollChannel pollChannel = PollUtil.getPollHandleErrors(event, getName());
        if (pollChannel == null)
            return;

        String description = OptionMappingParsing.getString("description", event, getName());
        if (description == null || description.length() > 2048) {
            if (description == null)
                event.replyEmbeds(Util.genericErrorEmbed("Error", "No description found")).setEphemeral(true).queue();
            else
                event.replyEmbeds(Util.genericErrorEmbed("Error", "Description too long")).setEphemeral(true).queue();
            return;
        }
        String finalDescription = description.replaceAll("/n", "\n");

        event.replyEmbeds(Util.genericWaitingEmbed("Waiting...", "Editing poll...")).setEphemeral(true).queue(hook -> {
            pollChannel.textChannel().retrieveMessageById(pollChannel.poll().getPollId()).queue(message -> updatePoll(message, finalDescription, hook),
                    error -> hook.editOriginalEmbeds(Util.genericErrorEmbed("Error", "Unable to find message with id [" + pollChannel.poll().getPollId() + "].")).queue());
        });
    }

    private void updatePoll(Message message, String description, InteractionHook hook) {
        EmbedBuilder firstEmbedBuilder = Util.getFirstEmbedBuilder(message);
        if (firstEmbedBuilder == null) {
            hook.editOriginalEmbeds(Util.genericErrorEmbed("Error", "Unable to get embed from poll message."))
                    .queue();
            return;
        }
        message.editMessageEmbeds(firstEmbedBuilder.setDescription(description).build()).queue(
                resultMessage -> hook.editOriginalEmbeds(Util.genericSuccessEmbed("Success", "Updated the poll description.")).queue(),
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
