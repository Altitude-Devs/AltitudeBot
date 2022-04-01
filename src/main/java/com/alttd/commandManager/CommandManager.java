package com.alttd.commandManager;

import com.alttd.commandManager.commands.CommandHelp;
import com.alttd.commandManager.commands.PollCommand.CommandPoll;
import com.alttd.database.Database;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandManager extends ListenerAdapter {

    private final List<DiscordCommand> commands;
    private final HashMap<String, List<ScopeInfo>> commandList = new HashMap<>();

    public CommandManager(JDA jda) {
        commands = List.of(new CommandHelp(jda, this),
                new CommandPoll(jda, this));
        loadCommands();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Optional<DiscordCommand> first = commands.stream()
                .filter(discordCommand -> discordCommand.getName().equalsIgnoreCase(event.getCommandString()))
                .findFirst();
        if (first.isEmpty()) {
            event.replyEmbeds(new EmbedBuilder()
                            .setTitle("Invalid command")
                            .setDescription("We couldn't find this command, please report this issue to a Teri")
                            .setColor(Color.RED)
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        first.get().execute(event);
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        Optional<DiscordCommand> first = commands.stream()
                .filter(discordCommand -> discordCommand.getName().equalsIgnoreCase(event.getCommandString()))
                .findFirst();
        if (first.isEmpty())
            return;
        first.get().suggest(event);
    }

    public List<DiscordCommand> getCommands() {
        return commands;
    }

    public List<DiscordCommand> getCommands(TextChannel textChannel) {
        return commands.stream().filter(command -> {
            List<ScopeInfo> scopeInfoList = commandList.get(command.getName());
            for (ScopeInfo scopeInfo : scopeInfoList) {
                switch (scopeInfo.getScope()) {
                    case GLOBAL -> {
                        return true;
                    }
                    case GUILD -> {
                        if (textChannel.getGuild().getIdLong() == scopeInfo.getId())
                            return true;
                    }
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

    private void loadCommands() {
        String sql = "SELECT * FROM commands";

        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String commandName = resultSet.getString("command_name");
                List<ScopeInfo> scopeInfoList = commandList.getOrDefault(commandName, new ArrayList<>());
                scopeInfoList.add(new ScopeInfo(
                        CommandScope.valueOf(resultSet.getString("scope")),
                        resultSet.getLong("location_id")));
                commandList.put(commandName, scopeInfoList);
            }
        } catch (SQLException exception) {
            Logger.sql(exception);
        }
    }

    public List<ScopeInfo> getActiveLocations(String command) {
        return commandList.getOrDefault(command, new ArrayList<>());
    }
}
