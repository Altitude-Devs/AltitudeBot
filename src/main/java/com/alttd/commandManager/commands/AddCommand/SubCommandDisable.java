package com.alttd.commandManager.commands.AddCommand;

import com.alttd.commandManager.*;
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
import java.util.stream.Collectors;

public class SubCommandDisable extends SubCommand {

    private final CommandManager commandManager;

    protected SubCommandDisable(CommandManager commandManager, SubCommandGroup parentGroup, DiscordCommand parent) {
        super(parentGroup, parent);
        this.commandManager = commandManager;
    }

    @Override
    public String getName() {
        return "disable";
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
        if (command == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to find a command with that name.")).setEphemeral(true).queue();
            return;
        }

        if (disableCommand(command, guild.getIdLong())) {
//            Util.deleteCommand(guild, guild.retri, command.getName()); //TODO add a way to disable commands?
            event.replyEmbeds(Util.genericSuccessEmbed("Disabled command",
                    Parser.parse("Successfully disabled <command> in <guild>!",
                            Template.of("command", commandName.toLowerCase()),
                            Template.of("guild", guild.getName())
                    ))).setEphemeral(true).queue();

        } else {
            event.replyEmbeds(Util.genericErrorEmbed("Failed to disable command",
                    Parser.parse("Unable to disable <command> in <guild>, is it already disabled?",
                            Template.of("command", commandName.toLowerCase()),
                            Template.of("guild", guild.getName())
                    ))).setEphemeral(true).queue();
        }
    }

    private boolean disableCommand(DiscordCommand command, long guildId) {
        if (!commandManager.disableCommand(command.getName(), new ScopeInfo(CommandScope.GUILD, guildId)))
            return false;
        String sql = "REMOVE FROM commands WHERE command_name = ? AND scope = ? and location_id = ?";
        PreparedStatement statement = null;

        try {
            statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setString(1, command.getName());
            statement.setString(2, "GUILD");
            statement.setLong(3, guildId);
            if (!statement.execute()) {
                Logger.warning("Unable to disable command: % for guild: %", command.getName(), String.valueOf(guildId));
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
                        .filter(name -> commandManager.getActiveLocations(name).contains(scopeInfo))
                        .limit(25)
                        .collect(Collectors.toList()))
                .queue();
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
