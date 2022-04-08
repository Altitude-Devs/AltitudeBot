package com.alttd.commandManager.commands.embed;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.permissions.PermissionManager;
import com.alttd.util.Logger;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.*;
import java.util.HashMap;

public class CommandEmbed extends DiscordCommand {

    private final HashMap<String, SubCommand> subCommandMap = new HashMap<>();

    public CommandEmbed(JDA jda, CommandManager commandManager) {
        SlashCommandData slashCommandData = Commands.slash(getName(), "A command to create embeds for polls etc")
                .addSubcommands(
                        new SubcommandData("add", "Add a new embed to a channel")
                                .addOption(OptionType.CHANNEL, "target", "The channel to send the new embed in", true)
                                .addOption(OptionType.STRING, "type", "Either `poll` or `default`")
                                .addOption(OptionType.STRING, "text", "The description for your poll, max 1000 characters"));
        slashCommandData.setDefaultEnabled(true);
        Util.registerCommand(commandManager, jda, slashCommandData, getName());
    }

    @Override
    public String getName() {
        return "embed";
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

        String subcommandName = event.getInteraction().getSubcommandName();
        if (subcommandName == null) {
            Logger.severe("No subcommand found for %", getName());
            return;
        }

        SubCommand subCommand = subCommandMap.get(subcommandName);
        if (subCommand == null) {
            event.replyEmbeds(Util.invalidSubcommand(subcommandName))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        subCommand.execute(event);
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {

    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
