package com.alttd.commandManager;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

public abstract class SubCommand extends SubOption{

    private final SubCommandGroup parentGroup;
    private final boolean inSubGroup;

    protected SubCommand(SubCommandGroup parentGroup, DiscordCommand parent) {
        super(parent);
        this.parentGroup = parentGroup;
        this.inSubGroup = parentGroup != null;
    }

    public SubCommandGroup getParentGroup() {
        return parentGroup;
    }

    public boolean isInSubGroup() {
        return inSubGroup;
    }

    @Override
    public String getPermission() {
        if (isInSubGroup())
            return getParentGroup().getPermission() + "." + getName();
        else
            return getParent().getPermission() + "." + getName();
    }

    public abstract void suggest(CommandAutoCompleteInteractionEvent event);
}
