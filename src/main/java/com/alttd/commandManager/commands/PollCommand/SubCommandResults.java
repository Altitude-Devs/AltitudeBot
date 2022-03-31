package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
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

    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
