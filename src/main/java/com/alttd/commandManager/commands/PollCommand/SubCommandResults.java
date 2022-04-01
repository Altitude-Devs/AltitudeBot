package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.util.OptionMappingParsing;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SubCommandResults extends SubCommand {
    protected SubCommandResults(DiscordCommand parent) {
        super(parent);
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
    public String getHelpMessage() {
        return null;
    }
}
