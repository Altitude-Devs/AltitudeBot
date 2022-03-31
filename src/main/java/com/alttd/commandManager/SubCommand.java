package com.alttd.commandManager;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class SubCommand {

    private final DiscordCommand parent;

    protected SubCommand(DiscordCommand parent) {
        this.parent = parent;
    }

    public DiscordCommand getParent() {
        return parent;
    }

    public abstract String getName();

    public String getPermission() {
        return getParent().getPermission() + "." +  getName();
    }

    public abstract void execute(SlashCommandInteractionEvent event);

    public abstract String getHelpMessage();

}
