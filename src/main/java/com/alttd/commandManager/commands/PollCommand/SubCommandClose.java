package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SubCommandClose extends SubCommand {
    protected SubCommandClose(DiscordCommand parent) {
        super(parent);
    }

    @Override
    public String getName() {
        return "close";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
