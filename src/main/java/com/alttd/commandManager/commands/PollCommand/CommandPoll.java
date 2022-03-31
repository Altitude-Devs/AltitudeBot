package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.ScopeInfo;
import com.alttd.permissions.PermissionManager;
import com.alttd.util.Logger;
import com.alttd.util.OptionMappingParsing;
import com.alttd.util.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.RestAction;

public class CommandPoll extends DiscordCommand {

    private final CommandManager commandManager;

    public CommandPoll(JDA jda, CommandManager commandManager) {
        this.commandManager = commandManager;
        SlashCommandData slashCommandData = Commands.slash(getName(), "Create, edit, and manage polls")
                .addSubcommands(
                        new SubcommandData("add", "Add a new poll to a channel")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll should go into", true, true)
                                .addOption(OptionType.STRING, "title", "Title of the embed (max 256 characters)", true),
                        new SubcommandData("edit_title", "Edit the title of a poll")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll is in", true, true)
                                .addOption(OptionType.INTEGER, "message_id", "Id of the poll you're editing", true)
                                .addOption(OptionType.STRING, "title", "The new title for the poll (max 256 characters)", true),
                        new SubcommandData("edit_description", "Edit the description of a poll")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll is in", true, true)
                                .addOption(OptionType.INTEGER, "message_id", "Id of the poll you're editing", true)
                                .addOption(OptionType.STRING, "description", "The new description for the poll (max 2048 characters)", true),
                        new SubcommandData("add_button", "Add a button to a poll")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll is in", true, true)
                                .addOption(OptionType.INTEGER, "message_id", "Id of the poll you're adding a button to", true)
                                .addOption(OptionType.INTEGER, "button_row", "Row the button should go in (1-5)", true)
                                .addOption(OptionType.STRING, "button_name", "Name of the button you're adding"),
                        new SubcommandData("remove_button", "Remove a button from a poll")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll is in", true, true)
                                .addOption(OptionType.INTEGER, "message_id", "Id of the poll you're removing a button from", true)
                                .addOption(OptionType.STRING, "button_name", "Name of the button you're removing"),
                        new SubcommandData("open", "Open a poll")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll is in", true, true)
                                .addOption(OptionType.INTEGER, "message_id", "Id of the poll you're opening", true),
                        new SubcommandData("close", "Close a poll")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll is in", true, true)
                                .addOption(OptionType.INTEGER, "message_id", "Id of the poll you're closing", true),
                        new SubcommandData("results", "Get the results for a poll")
                                .addOption(OptionType.CHANNEL, "channel", "Channel this poll is in", true, true)
                                .addOption(OptionType.INTEGER, "message_id", "Id of the poll you want the results for", true));
        for (ScopeInfo info : commandManager.getActiveLocations(getName())) {
            switch (info.getScope()) {
                case GLOBAL -> jda.updateCommands().addCommands(slashCommandData).queue();
                case GUILD -> {
                    Guild guildById = jda.getGuildById(info.getId());
                    if (guildById == null)
                    {
                        Logger.warning("Tried to add command % to invalid guild %", getName(), String.valueOf(info.getId()));
                        continue;
                    }
                    guildById.updateCommands().addCommands(slashCommandData).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
                }
                case USER -> Logger.warning("Tried to add command % to user, this is not implemented yet since I don't know how this should work.");
            }
        }
    }

    @Override
    public String getName() {
        return "poll";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null || event.getMember() == null)
        {
            event.replyEmbeds(Util.guildOnlyCommand(getName())).setEphemeral(true).queue();
            return;
        }
        if (PermissionManager.getInstance().hasPermission(event.getTextChannel(), event.getMember(), getPermission())) {
            event.replyEmbeds(Util.noPermission(getName())).setEphemeral(true).queue();
            return;
        }

        String subcommandName = event.getInteraction().getSubcommandName();
        if (subcommandName == null) {
            Logger.severe("No subcommand found for %", getName());
            return;
        }

        switch (subcommandName) {
            case "add" -> {
                GuildMessageChannel channel = OptionMappingParsing.getGuildChannel("channel", event, getName());
                if (channel == null)
                    return;
                String title = OptionMappingParsing.getString("title", event, getName());
                if (title == null)
                    return;
            }
            case "edit_title" -> {
                GuildMessageChannel channel = OptionMappingParsing.getGuildChannel("channel", event, getName());
                if (channel == null)
                    return;
                Long messageId = OptionMappingParsing.getLong("message_id", event, getName());
                if (messageId == null)
                    return;
                String title = OptionMappingParsing.getString("title", event, getName());
                if (title == null)
                    return;
            }
            case "edit_description" -> {
                GuildMessageChannel channel = OptionMappingParsing.getGuildChannel("channel", event, getName());
                if (channel == null)
                    return;
                Long messageId = OptionMappingParsing.getLong("message_id", event, getName());
                if (messageId == null)
                    return;
                String description = OptionMappingParsing.getString("description", event, getName());
                if (description == null)
                    return;
            }
            case "add_button" -> {
                GuildMessageChannel channel = OptionMappingParsing.getGuildChannel("channel", event, getName());
                if (channel == null)
                    return;
                Long messageId = OptionMappingParsing.getLong("message_id", event, getName());
                if (messageId == null)
                    return;
                Long rowLong = OptionMappingParsing.getLong("button_row", event, getName());
                if (rowLong == null)
                    return;
                int row = rowLong.intValue();
                String buttonName = OptionMappingParsing.getString("button_name", event, getName());
                if (buttonName == null)
                    return;
            }
            case "remove_button" -> {
                GuildMessageChannel channel = OptionMappingParsing.getGuildChannel("channel", event, getName());
                if (channel == null)
                    return;
                Long messageId = OptionMappingParsing.getLong("message_id", event, getName());
                if (messageId == null)
                    return;
                String buttonName = OptionMappingParsing.getString("button_name", event, getName());
                if (buttonName == null)
                    return;
            }
            case "open" -> {
                GuildMessageChannel channel = OptionMappingParsing.getGuildChannel("channel", event, getName());
                if (channel == null)
                    return;
                Long messageId = OptionMappingParsing.getLong("message_id", event, getName());
                if (messageId == null)
                    return;
            }
            case "close" -> {
                GuildMessageChannel channel = OptionMappingParsing.getGuildChannel("channel", event, getName());
                if (channel == null)
                    return;
                Long messageId = OptionMappingParsing.getLong("message_id", event, getName());
                if (messageId == null)
                    return;
            }
            case "results" -> {
                GuildMessageChannel channel = OptionMappingParsing.getGuildChannel("channel", event, getName());
                if (channel == null)
                    return;
                Long messageId = OptionMappingParsing.getLong("message_id", event, getName());
                if (messageId == null)
                    return;
            }
            default -> throw new IllegalStateException("Unexpected value: " + subcommandName);
        }
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
