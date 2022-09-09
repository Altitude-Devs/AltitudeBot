package com.alttd.commandManager.commands.AddCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommandGroup;
import com.alttd.commandManager.SubOption;
import com.alttd.permissions.PermissionManager;
import com.alttd.util.Logger;
import com.alttd.util.Util;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.HashMap;

public class SubCommandGroupUnset extends SubCommandGroup {

    private final HashMap<String, SubOption> subOptionsMap = new HashMap<>();

    protected SubCommandGroupUnset(DiscordCommand parent) {
        super(parent);
        Util.registerSubOptions(subOptionsMap,
                new SubCommandUnsetGroup(this, getParent()),
                new SubCommandUnsetUser(this, getParent()));
    }

    @Override
    public String getName() {
        return "unset";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (PermissionManager.getInstance().hasPermission(event.getChannel().asTextChannel(), event.getIdLong(), Util.getGroupIds(event.getMember()), getPermission())) {
            event.replyEmbeds(Util.noPermission(getName())).setEphemeral(true).queue();
            return;
        }

        String subcommandName = event.getInteraction().getSubcommandName();
        if (subcommandName == null) {
            Logger.severe("No subcommand found for %", getName());
            return;
        }

        SubOption subOption = subOptionsMap.get(subcommandName);
        if (subOption == null) {
            event.replyEmbeds(Util.invalidSubcommand(subcommandName))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        subOption.execute(event);
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
