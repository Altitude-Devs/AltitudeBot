package com.alttd.commandManager.commands.AddCommand;

import com.alttd.commandManager.*;
import com.alttd.contextMenuManager.ContextMenuManager;
import com.alttd.contextMenuManager.DiscordContextMenu;
import com.alttd.database.Database;
import com.alttd.templates.Parser;
import com.alttd.templates.Template;
import com.alttd.util.Logger;
import com.alttd.util.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SubCommandEnable extends SubCommand {

    private final CommandManager commandManager;
    private final ContextMenuManager contextMenuManager;

    protected SubCommandEnable(CommandManager commandManager, ContextMenuManager contextMenuManager, SubCommandGroup parentGroup, DiscordCommand parent) {
        super(parentGroup, parent);
        this.commandManager = commandManager;
        this.contextMenuManager = contextMenuManager;
    }

    @Override
    public String getName() {
        return "enable";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This command can only be used within guilds")).setEphemeral(true).queue();
            return;
        }

        OptionMapping option = event.getOption("command");
        if (option == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to find command parameter.")).setEphemeral(true).queue();
            return;
        }

        String commandName = option.getAsString();
        DiscordCommand command = commandManager.getCommand(commandName);
        if (command != null) {
            tryEnableCommand(command, guild, commandName, event);
            return;
        }

        DiscordContextMenu contextMenu = contextMenuManager.getContext(commandName);
        if (contextMenu != null) {
            tryEnableContextMenu(contextMenu, guild, commandName, event);
            //todo stuff
            return;
        }

        event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to find a command called [" + commandName + "].")).setEphemeral(true).queue();
    }

    private void tryEnableCommand(DiscordCommand command, Guild guild, String commandName, SlashCommandInteractionEvent event) {
        if (enableCommand(command, guild.getIdLong())) {
            Util.registerCommand(guild, command.getCommandData(), command.getName());
            event.replyEmbeds(Util.genericSuccessEmbed("Enabled command",
                    Parser.parse("Successfully enabled <command> in <guild>!",
                            Template.of("command", commandName.toLowerCase()),
                            Template.of("guild", guild.getName())
                    ))).setEphemeral(true).queue();
        } else {
            event.replyEmbeds(Util.genericErrorEmbed("Failed to enable command",
                    Parser.parse("Unable to enable <command> in <guild>, is it already enabled?",
                            Template.of("command", commandName.toLowerCase()),
                            Template.of("guild", guild.getName())
                    ))).setEphemeral(true).queue();
        }
    }

    private void tryEnableContextMenu(DiscordContextMenu contextMenu, Guild guild, String commandName, SlashCommandInteractionEvent event) {
        if (enableContextMenu(contextMenu, guild.getIdLong())) {
            Util.registerCommand(guild, contextMenu.getUserContextInteraction(), contextMenu.getContextMenuId());
            event.replyEmbeds(Util.genericSuccessEmbed("Enabled command",
                    Parser.parse("Successfully enabled <command> in <guild>!",
                            Template.of("command", commandName.toLowerCase()),
                            Template.of("guild", guild.getName())
                    ))).setEphemeral(true).queue();
        } else {
            event.replyEmbeds(Util.genericErrorEmbed("Failed to enable command",
                    Parser.parse("Unable to enable <command> in <guild>, is it already enabled?",
                            Template.of("command", commandName.toLowerCase()),
                            Template.of("guild", guild.getName())
                    ))).setEphemeral(true).queue();
        }
    }

    private boolean enableCommand(DiscordCommand command, long guildId) {
        if (!commandManager.enableCommand(command.getName(), new ScopeInfo(CommandScope.GUILD, guildId)))
            return false;
        String sql = "INSERT INTO commands (command_name, scope, location_id) VALUES(?, ?, ?)";
        PreparedStatement statement = null;

        try {
            statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setString(1, command.getName());
            statement.setString(2, "GUILD");
            statement.setLong(3, guildId);
            if (statement.executeUpdate() == 0) {
                Logger.warning("Unable to enable command: % for guild: %", command.getName(), String.valueOf(guildId));
                return false;
            }
        } catch (SQLException exception) {
            Logger.sql(exception);
            return false;
        } finally {
            try {
                if (statement != null)
                    statement.close();
            } catch (SQLException exception) {
                Logger.sql(exception);
            }
        }
        return true;
    }

    private boolean enableContextMenu(DiscordContextMenu contextMenu, long guildId) {
        if (!commandManager.enableCommand(contextMenu.getContextMenuId(), new ScopeInfo(CommandScope.GUILD, guildId)))
            return false;
        String sql = "INSERT INTO commands (command_name, scope, location_id) VALUES(?, ?, ?)";
        PreparedStatement statement = null;

        try {
            statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setString(1, contextMenu.getContextMenuId());
            statement.setString(2, "GUILD");
            statement.setLong(3, guildId);
            if (statement.executeUpdate() == 0) {
                Logger.warning("Unable to enable command: % for guild: %", contextMenu.getContextMenuId(), String.valueOf(guildId));
                return false;
            }
        } catch (SQLException exception) {
            Logger.sql(exception);
            return false;
        } finally {
            try {
                if (statement != null)
                    statement.close();
            } catch (SQLException exception) {
                Logger.sql(exception);
            }
        }
        return true;
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        OptionMapping option = event.getOption("command");
        Guild guild = event.getGuild();
        if (guild == null || option == null) {
            event.replyChoiceStrings(new ArrayList<>()).queue();
            return;
        }
        String commandName = option.getAsString().toLowerCase();
        ScopeInfo scopeInfo = new ScopeInfo(CommandScope.GLOBAL, event.getGuild().getIdLong());
        List<String> collect = commandManager.getCommands().stream()
                .map(DiscordCommand::getName)
                .filter(name -> name.toLowerCase().startsWith(commandName))
                .filter(name -> !commandManager.getActiveLocations(name).contains(scopeInfo))
                .limit(25)
                .collect(Collectors.toList());

        collect.addAll(contextMenuManager.getContexts().stream()
                .map(DiscordContextMenu::getContextMenuId)
                .filter(name -> name.toLowerCase().startsWith(commandName))
                .filter(name -> !commandManager.getActiveLocations(name).contains(scopeInfo))
                .limit(25)
                .collect(Collectors.toList()));

        event.replyChoiceStrings(collect).queue();
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
