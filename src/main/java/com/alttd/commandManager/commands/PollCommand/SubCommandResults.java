package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.commandManager.SubCommandGroup;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SubCommandResults extends SubCommand {
    protected SubCommandResults(SubCommandGroup parentGroup, DiscordCommand parent) {
        super(parentGroup, parent);
    }

    @Override
    public String getName() {
        return "results";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        PollChannel pollChannel = PollUtil.getPollHandleErrors(event, getName());
        if (pollChannel == null)
            return;
        event.replyEmbeds(pollChannel.poll().getVotesEmbed()).setEphemeral(true).queue();
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
