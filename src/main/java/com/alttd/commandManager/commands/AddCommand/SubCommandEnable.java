package com.alttd.commandManager.commands.AddCommand;

import com.alttd.commandManager.*;
import com.alttd.database.Database;
import com.alttd.templates.Parser;
import com.alttd.templates.Template;
import com.alttd.util.Logger;
import com.alttd.util.Util;
import com.google.protobuf.GeneratedMessageV3;
import com.mysql.cj.log.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SubCommandEnable extends SubCommand {

    private final CommandManager commandManager;

    protected SubCommandEnable(CommandManager commandManager, SubCommandGroup parentGroup, DiscordCommand parent) {
        super(parentGroup, parent);
        this.commandManager = commandManager;
    }

    @Override
    public String getName() {
        return "enable";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This command can only be used within guilds")).queue();
            return;
        }

        OptionMapping option = event.getOption("command");
        if (option == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to find command parameter.")).queue();
            return;
        }

        String commandName = option.getAsString();
        DiscordCommand command = commandManager.getCommand(commandName);
        if (command == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to find a command with that name.")).queue();
            return;
        }

        if (enableCommand(command, guild.getIdLong())) {
            event.replyEmbeds(Util.genericSuccessEmbed("Enabled command",
                    Parser.parse("Successfully enabled <command> in <guild>!",
                            Template.of("command", commandName.toLowerCase()),
                            Template.of("guild", guild.getName())
                    ))).queue();

        } else {
            event.replyEmbeds(Util.genericErrorEmbed("Failed to enable command",
                    Parser.parse("Unable to enabled <command> in <guild>, is it already enabled?",
                            Template.of("command", commandName.toLowerCase()),
                            Template.of("guild", guild.getName())
                    ))).queue();
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
            if (!statement.execute()) {
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
        event.replyChoiceStrings(commandManager.getCommands().stream()
                        .map(DiscordCommand::getName)
                        .filter(name -> name.toLowerCase().startsWith(commandName))
                        .filter(name -> !commandManager.getActiveLocations(name).contains(scopeInfo))
                        .limit(25)
                        .collect(Collectors.toList()))
                .queue();
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
