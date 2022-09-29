package com.alttd.contextMenuManager;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class DiscordContextMenu {

    public abstract String getContextMenuId();

    public abstract void execute(UserContextInteractionEvent event);

    public abstract void execute(MessageContextInteractionEvent event);

    public abstract CommandData getUserContextInteraction();

}
