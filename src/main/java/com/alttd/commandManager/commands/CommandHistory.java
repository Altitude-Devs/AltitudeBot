package com.alttd.commandManager.commands;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.database.queries.QueriesHistory.History;
import com.alttd.database.queries.QueriesHistory.HistoryType;
import com.alttd.database.queries.QueriesHistory.QueriesHistory;
import com.alttd.database.queries.QueriesUserUUID;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CommandHistory extends DiscordCommand {

    private final CommandData commandData;

    public CommandHistory(JDA jda, CommandManager commandManager) {
        this.commandData = Commands.slash(getName(), "Show history for a user")
                .addOption(OptionType.STRING, "user", "The user to show history for", true)
                .addOption(OptionType.STRING, "type", "The type of punishment to show", false, true)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);

        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "history";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping option = event.getInteraction().getOption("user");
        if (option == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to find user option"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        String username = option.getAsString();

        if (username.length() == 36)
            histUUID(username, event);
        else
            histName(username, event);
    }

    private void histUUID(String username, SlashCommandInteractionEvent event) {
        UUID uuid;
        try {
            uuid = UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Invalid UUID"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        List<History> historyList = QueriesHistory.getHistory(uuid);

        if (historyList == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve history for user"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        sendHistEmbed(event, historyList, uuid);
    }

    private void histName(String username, SlashCommandInteractionEvent event) {
        if (!username.matches("^[a-zA-Z0-9_]{2,16}$")) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Invalid username"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        UUID uuid = QueriesUserUUID.getUUIDByUsername(username);
        if (uuid == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Could not find uuid for username"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        List<History> history = QueriesHistory.getHistory(uuid);

        if (history == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve history for user"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        sendHistEmbed(event, history, username);
    }

    private void sendHistEmbed(SlashCommandInteractionEvent event, List<History> historyList, UUID uuid) {
        String username = QueriesUserUUID.getUsernameByUUID(uuid);
        if (username == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve username for uuid"))
                    .setEphemeral(true).queue();
            return;
        }
        sendHistEmbed(event, historyList, username);
    }

    private void sendHistEmbed(SlashCommandInteractionEvent event, List<History> historyList, String username) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("History for: " + username);

        int i = 0;
        for (History history : historyList) {
            if (i == 24)
                break;
            embedBuilder.addField(getHistoryTitle(history), getHistoryDescription(username, history), false);
            i++;
        }
        event.replyEmbeds(embedBuilder.build())
                .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
    }

    private String getHistoryTitle(History history) {
        String title = history.historyType().name().toLowerCase();
        return title.substring(0, 1).toUpperCase() + title.substring(1);
    }

    private String getHistoryDescription(String username, History history) {
        String timeAgo = TimeUnit.MILLISECONDS.toDays(new Date().getTime() - history.time()) + " days ago";
        String punishmentLength;
        long pl = TimeUnit.MILLISECONDS.toDays(history.until() - history.time());
        if (pl == 0) {
            pl = TimeUnit.MILLISECONDS.toHours(history.until() - history.time());
            punishmentLength = pl + " hours";
        } else
            punishmentLength = pl + " days";
        return "`" + username + "` executed by `" + history.bannedBy() + "`\nReason: " + history.reason() + "\nFor: " + punishmentLength + ", " + timeAgo + ".";
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        OptionMapping option = event.getOption("type");
        Guild guild = event.getGuild();
        if (guild == null || option == null) {
            event.replyChoiceStrings(new ArrayList<>()).queue();
            return;
        }
        String type = option.getAsString().toUpperCase();
        event.replyChoiceStrings(Arrays.stream(HistoryType.values()).map(HistoryType::name).collect(Collectors.toList()))
                .queue(RestAction.getDefaultSuccess(), Util::handleFailure);
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
