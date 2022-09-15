package com.alttd.commandManager.commands;

import com.alttd.commandManager.DiscordCommand;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class CommandEvidence extends DiscordCommand {
    @Override
    public String getName() {
        return "evidence";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {

    }

    @Override
    public String getHelpMessage() {
        return null;
    }

    @Override
    public CommandData getCommandData() {
        return null;
    }
}
