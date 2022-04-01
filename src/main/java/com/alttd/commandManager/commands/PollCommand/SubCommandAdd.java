package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.util.OptionMappingParsing;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SubCommandAdd extends SubCommand {

    protected SubCommandAdd(DiscordCommand parent) {
        super(parent);
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildMessageChannel channel = OptionMappingParsing.getGuildChannel("channel", event, getName());
        if (channel == null)
            return;
        String title = OptionMappingParsing.getString("title", event, getName());
        if (title == null)
            return;
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
