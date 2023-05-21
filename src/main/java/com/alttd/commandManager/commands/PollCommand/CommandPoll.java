package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubOption;
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

import java.util.HashMap;

public class CommandPoll extends DiscordCommand {

    private final HashMap<String, SubOption> subOptionsMap = new HashMap<>();
    private final CommandData commandData;

    public CommandPoll(JDA jda, CommandManager commandManager) {
        commandData = Commands.slash(getName(), "Create, edit, and manage polls")
                .addSubcommands(
                        new SubcommandData("add", "Add a new poll to a channel")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll should go into", true)
                                .addOption(OptionType.STRING, "title", "Title of the embed (max 256 characters)", true),
                        new SubcommandData("edit_title", "Edit the title of a poll")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll is in", true)
                                .addOption(OptionType.STRING, "message_id", "Id of the poll you're editing", true)
                                .addOption(OptionType.STRING, "title", "The new title for the poll (max 256 characters)", true),
                        new SubcommandData("edit_description", "Edit the description of a poll")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll is in", true)
                                .addOption(OptionType.STRING, "message_id", "Id of the poll you're editing", true)
                                .addOption(OptionType.STRING, "description", "The new description for the poll (max 2048 characters)", true),
                        new SubcommandData("add_button", "Add a button to a poll")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll is in", true)
                                .addOption(OptionType.STRING, "message_id", "Id of the poll you're adding a button to", true)
                                .addOption(OptionType.INTEGER, "button_row", "Row the button should go in (1-5)", true)
                                .addOption(OptionType.STRING, "button_name", "Name of the button you're adding"),
                        new SubcommandData("remove_button", "Remove a button from a poll")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll is in", true)
                                .addOption(OptionType.STRING, "message_id", "Id of the poll you're removing a button from", true)
                                .addOption(OptionType.STRING, "button_name", "Name of the button you're removing"),
                        new SubcommandData("open", "Open a poll")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll is in", true)
                                .addOption(OptionType.STRING, "message_id", "Id of the poll you're opening", true),
                        new SubcommandData("close", "Close a poll")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll is in", true)
                                .addOption(OptionType.STRING, "message_id", "Id of the poll you're closing", true),
                        new SubcommandData("results", "Get the results for a poll")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll is in", true)
                                .addOption(OptionType.STRING, "message_id", "Id of the poll you want the results for", true))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .setGuildOnly(true);
        Util.registerSubOptions(subOptionsMap,
                new SubCommandAdd(null,this),
                new SubCommandAddButton(null, this),
                new SubCommandClose(null,this),
                new SubCommandEditDescription(null, this),
                new SubCommandEditTitle(null, this),
                new SubCommandOpen(null, this),
                new SubCommandRemoveButton(null, this),
                new SubCommandResults(null,this));
        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "poll";
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
