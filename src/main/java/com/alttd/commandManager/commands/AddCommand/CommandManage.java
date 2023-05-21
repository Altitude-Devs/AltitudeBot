package com.alttd.commandManager.commands.AddCommand;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.commandManager.SubOption;
import com.alttd.contextMenuManager.ContextMenuManager;
import com.alttd.util.Logger;
import com.alttd.util.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.ArrayList;
import java.util.HashMap;

public class CommandManage extends DiscordCommand {
    private final HashMap<String, SubOption> subOptionsMap = new HashMap<>();
    private final CommandData commandData;

    public CommandManage(JDA jda, CommandManager commandManager, ContextMenuManager contextMenuManager) {
        commandData = Commands.slash(getName(), "Enable commands and assign permissions")
                .addSubcommands(
                        new SubcommandData("enable", "Enable a command in a channel")
                                .addOption(OptionType.STRING, "command", "Name of the command to enable", true, true),
                        new SubcommandData("disable", "Disable a command")
                                .addOption(OptionType.STRING, "command", "Name of the command to disable", true, true)
                        )
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .setGuildOnly(true);
        Util.registerSubOptions(subOptionsMap,
                new SubCommandEnable(commandManager, contextMenuManager, null, this),
                new SubCommandDisable(commandManager, null, this)
        );
        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "manage";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null || event.getMember() == null) {
            event.replyEmbeds(Util.guildOnlyCommand(getName())).setEphemeral(true).queue();
            return;
        }

        String subcommandName = event.getInteraction().getSubcommandGroup();
        subcommandName = subcommandName == null ? event.getInteraction().getSubcommandName() : subcommandName;
        if (subcommandName == null) {
            Logger.altitudeLogs.error("No subcommand found for " + getName());
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
        SubOption subOption = subOptionsMap.get(event.getSubcommandName());
        if (subOption instanceof SubCommand subCommand)
            subCommand.suggest(event);
        else
            event.replyChoices(new ArrayList<>()).queue();
    }

    @Override
    public String getHelpMessage() {
        return null;
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }
}
