package com.alttd.commandManager;

import com.alttd.commandManager.commands.AddCommand.CommandManage;
import com.alttd.commandManager.commands.*;
import com.alttd.commandManager.commands.PollCommand.CommandPoll;
import com.alttd.contextMenuManager.ContextMenuManager;
import com.alttd.database.Database;
import com.alttd.listeners.ChatListener;
import com.alttd.modalManager.ModalManager;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
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

    public CommandManager(JDA jda, ModalManager modalManager, ContextMenuManager contextMenuManager, ChatListener chatListener) {
        commandList.put("manage", new ArrayList<>(List.of(new ScopeInfo(CommandScope.GLOBAL, 0))));
        loadCommands();
        Logger.info("Loading commands...");
        CommandSetToggleableRoles commandSetToggleableRoles = new CommandSetToggleableRoles(jda, this);
        commands = List.of(
                new CommandManage(jda, this, contextMenuManager),
                new CommandHelp(jda, this),
                new CommandPoll(jda, this),
                new CommandSuggestion(jda, modalManager, this),
                new CommandSuggestCrateItem(jda, modalManager, this),
                new CommandSetOutputChannel(jda, this),
                new CommandUpdateCommands(jda, this),
                new CommandEvidence(jda, modalManager, this),
                new CommandFlag(jda, this),
                new CommandHistory(jda, this),
                new CommandSeen(jda, this),
                commandSetToggleableRoles,
                new CommandToggleRole(commandSetToggleableRoles, jda, this),
                new CommandRemindMe(jda, this, modalManager),
                new CommandSoftLock(jda, this, chatListener));
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        Optional<DiscordCommand> first = commands.stream()
                .filter(discordCommand -> discordCommand.getName().equalsIgnoreCase(commandName))
                .findFirst();
        if (first.isEmpty()) {
            event.replyEmbeds(new EmbedBuilder()
                            .setTitle("Invalid command")
                            .setDescription("We couldn't find a command called [" + commandName + "], please report this issue to a Teri")
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
                .filter(discordCommand -> discordCommand.getName().equalsIgnoreCase(event.getName()))
                .findFirst();
        if (first.isEmpty())
            return;
        first.get().suggest(event);
    }

    public DiscordCommand getCommand(String name) {
        for (DiscordCommand command : commands) {
            if (command.getName().equalsIgnoreCase(name))
                return command;
        }
        return null;
    }

    public List<DiscordCommand> getCommands() {
        return commands;
    }

    public List<DiscordCommand> getCommands(Guild guild) {
        return commands.stream().filter(command -> {
            List<ScopeInfo> scopeInfoList = commandList.get(command.getName());
            if (scopeInfoList == null)
                return false;
            for (ScopeInfo scopeInfo : scopeInfoList) {
                switch (scopeInfo.getScope()) {
                    case GLOBAL -> {
                        return true;
                    }
                    case GUILD -> {
                        if (guild.getIdLong() == scopeInfo.getId())
                            return true;
                    }
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

    public boolean enableCommand(String commandName, ScopeInfo scopeInfo) {
        List<ScopeInfo> scopeInfoList = commandList.getOrDefault(commandName, new ArrayList<>());
        scopeInfoList.add(scopeInfo);
        commandList.put(commandName, scopeInfoList);
        return true;
    }

    public boolean disableCommand(String commandName, ScopeInfo scopeInfo) {
        List<ScopeInfo> scopeInfoList = commandList.get(commandName);
        if (scopeInfoList == null)
            return false;
        if (!scopeInfoList.contains(scopeInfo))
            return false;
        scopeInfoList.remove(scopeInfo);
        if (scopeInfoList.isEmpty())
            commandList.remove(commandName);
        return true;
    }

    synchronized private void loadCommands() {
        String sql = "SELECT * FROM commands";
        PreparedStatement statement = null;

        try {
            statement = Database.getDatabase().getConnection().prepareStatement(sql);
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
        } finally {
            try {
                if (statement != null)
                    statement.close();
            } catch (SQLException exception) {
                Logger.sql(exception);
            }
        }
    }

    synchronized public List<ScopeInfo> getActiveLocations(String command) {
        return commandList.getOrDefault(command, new ArrayList<>());
    }
}
