package com.alttd.commandManager;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class DiscordCommand {

    public abstract String getName();

    public String getPermission() {
        return "command." + getName();
    }

    public abstract void execute(SlashCommandInteractionEvent event);

    public abstract void suggest(CommandAutoCompleteInteractionEvent event);

    public abstract String getHelpMessage();

    public String getExtendedHelpMessage() {
        return getHelpMessage();
    }

}
