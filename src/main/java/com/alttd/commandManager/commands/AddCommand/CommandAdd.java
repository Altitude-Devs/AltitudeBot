package com.alttd.commandManager.commands.AddCommand;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubOption;
import com.alttd.permissions.PermissionManager;
import com.alttd.util.Logger;
import com.alttd.util.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.util.HashMap;

public class CommandAdd extends DiscordCommand {
    private final HashMap<String, SubOption> subOptionsMap = new HashMap<>();

    public CommandAdd(JDA jda, CommandManager commandManager) {
        SlashCommandData slashCommandData = Commands.slash(getName(), "Enable commands and assign permissions")
                .addSubcommandGroups(
                        new SubcommandGroupData("set", "Set a permission for a user")
                                .addSubcommands(
                                        new SubcommandData("user", "Set a permission for a user")
                                                .addOption(OptionType.MENTIONABLE, "user", "The user to set the permission for", true)
                                                .addOption(OptionType.STRING, "permission", "The permission to set for a user", true, true)
                                                .addOption(OptionType.STRING, "state", "To allow or deny the permission (true/false)", true, true),
                                        new SubcommandData("group", "Set a permission for a group")
                                                .addOption(OptionType.MENTIONABLE, "group", "The group to set the permission for", true)
                                                .addOption(OptionType.STRING, "permission", "The permission to set for a group", true, true)
                                                .addOption(OptionType.STRING, "state", "To allow or deny the permission (true/false)", true, true)
                                ),
                        new SubcommandGroupData("unset", "unset a permission for a user")
                                .addSubcommands(
                                        new SubcommandData("user", "Unset a permission for a user")
                                                .addOption(OptionType.MENTIONABLE, "user", "The user to unset the permission for", true)
                                                .addOption(OptionType.STRING, "permission", "The permission to unset for a user", true, true),
                                        new SubcommandData("group", "Unset a permission for a group")
                                                .addOption(OptionType.MENTIONABLE, "group", "The group to unset the permission for", true)
                                                .addOption(OptionType.STRING, "permission", "The permission to unset for a group", true, true)
                                )
                )
                .addSubcommands(
                        new SubcommandData("enable", "Enable a command in a channel")
                                .addOption(OptionType.CHANNEL, "channel", "Channel to enable this command in", true)
                                .addOption(OptionType.STRING, "command", "Name of the command to enable", true, true),
                        new SubcommandData("disable", "Disable a command")
                                .addOption(OptionType.CHANNEL, "channel", "Channel to disable this command in", true)
                                .addOption(OptionType.STRING, "command", "Name of the command to disable", true, true)
                        );
        slashCommandData.setDefaultEnabled(true);
        Util.registerSubOptions(subOptionsMap,
                new SubCommandDisable(commandManager, null, this),
                new SubCommandEnable(commandManager, null, this));
        Util.registerCommand(commandManager, jda, slashCommandData, getName());
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null || event.getMember() == null) {
            event.replyEmbeds(Util.guildOnlyCommand(getName())).setEphemeral(true).queue();
            return;
        }
        if (PermissionManager.getInstance().hasPermission(event.getTextChannel(), event.getIdLong(), Util.getGroupIds(event.getMember()), getPermission())) {
            event.replyEmbeds(Util.noPermission(getName())).setEphemeral(true).queue();
            return;
        }

        String subcommandName = event.getInteraction().getSubcommandGroup();
        subcommandName = subcommandName == null ? event.getInteraction().getSubcommandName() : subcommandName;
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
    public void suggest(CommandAutoCompleteInteractionEvent event) {

    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
