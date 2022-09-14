package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.commandManager.SubCommandGroup;
import com.alttd.util.OptionMappingParsing;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
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
        GuildMessageChannel channel = OptionMappingParsing.getGuildChannel("channel", event, getName());
        if (channel == null)
            return;
        Long messageId = OptionMappingParsing.getLong("message_id", event, getName());
        if (messageId == null)
            return;
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {

    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
