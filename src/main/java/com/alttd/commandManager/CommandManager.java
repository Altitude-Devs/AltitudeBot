package com.alttd.commandManager;

import com.alttd.database.Database;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CommandManager extends ListenerAdapter {

    private final List<DiscordCommand> commands;

    public CommandManager() {
        commands = List.of();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

    }

    public List<DiscordCommand> getCommands() {
        return commands;
    }

    public List<ScopeInfo> getActiveLocations(String command) { //TODO make this cache results
        String sql = "SELECT FROM commands WHERE command_name = ?";
        List<ScopeInfo> scopeInfoList = new ArrayList<>();

        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setString(1, command.toLowerCase());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                scopeInfoList.add(new ScopeInfo(
                        CommandScope.valueOf(resultSet.getString("scope")),
                        resultSet.getLong("location_id")));
            }
        } catch (SQLException exception) {
            Logger.sql(exception);
        }
        return scopeInfoList;
    }
}
