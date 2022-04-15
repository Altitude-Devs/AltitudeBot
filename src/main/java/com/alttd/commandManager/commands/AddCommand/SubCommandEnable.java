package com.alttd.commandManager.commands.AddCommand;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.commandManager.SubCommandGroup;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SubCommandEnable extends SubCommand {

    private final CommandManager commandManager;

    protected SubCommandEnable(CommandManager commandManager, SubCommandGroup parentGroup, DiscordCommand parent) {
        super(parentGroup, parent);
        this.commandManager = commandManager;
    }

    @Override
    public String getName() {
        return "enable";
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
}
