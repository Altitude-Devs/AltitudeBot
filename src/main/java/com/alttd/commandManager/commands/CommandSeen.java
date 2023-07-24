package com.alttd.commandManager.commands;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.config.MessagesConfig;
import com.alttd.database.queries.QueriesUserUUID;
import com.alttd.database.queries.queriesSeen.PlaytimeSeen;
import com.alttd.database.queries.queriesSeen.SeenQueries;
import com.alttd.templates.Parser;
import com.alttd.templates.Template;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CommandSeen extends DiscordCommand {

    private final CommandData commandData;
    private static final List<String> validServers = List.of("lobby", "creative", "grove");

    public CommandSeen(JDA jda, CommandManager commandManager) {
        commandData = Commands.slash(getName(), "Check when a player was last online.")
                .addOption(OptionType.STRING, "playername", "The playername or uuid you want to check.", true, false)
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);

        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "seen";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String playerName = event.getOption("playername", OptionMapping::getAsString);
        if (playerName == null || playerName.length() < 3 || playerName.length() > 16) {
            error(event);
            return;
        }

        UUID uuid = QueriesUserUUID.getUUIDByUsername(playerName);
        if (uuid == null) {
            error(event);
            return;
        }

        List<PlaytimeSeen> lastSeen = SeenQueries.getLastSeen(uuid);
        if (lastSeen == null || lastSeen.isEmpty()) {
            error(event);
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder
                .setTitle("Seen")
                .setColor(Color.GREEN)
                .appendDescription("`" + Util.capitalize(playerName) + "`'s seen information per server:\n\n");
        for (PlaytimeSeen playtimeSeen : lastSeen) {
            if (playtimeSeen == null || playtimeSeen.getLastSeen() == 0 || !validServers.contains(playtimeSeen.getServer().toLowerCase()))
                continue;
            embedBuilder.appendDescription(Util.capitalize(playtimeSeen.getServer()) + ": " + "<t:" + TimeUnit.MILLISECONDS.toSeconds(playtimeSeen.getLastSeen()) +":R>\n");
        }

        event.replyEmbeds(embedBuilder.build())
                .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(Collections.emptyList())
                .queue(RestAction.getDefaultSuccess(), Util::handleFailure);
    }

    @Override
    public String getHelpMessage() {
        return MessagesConfig.HELP_SEEN;
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }

    private static String getPassedTime(Long time) {
        return Util.convertTime(new Date().getTime() - time);
    }

    private void error(SlashCommandInteractionEvent event) {
        event.replyEmbeds(Util.genericErrorEmbed("Error", "No recorded time."))
                .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
    }

//        private static Component getOnlineSeen(String playerName, Player player) {
//        UUID uuid = player.getUniqueId();
//        PlaytimePlayer playtimePlayer = Queries.getPlaytimePlayer(uuid);
//
//        if (playtimePlayer == null) return MiniMessage.get().parse(Config.Messages.PLAYER_NOT_FOUND.getMessage().replaceAll("%player%", playerName));
//
//        return getOnlineSeen(playtimePlayer, player);
//    }
//    private static Component getOfflineSeen(String playerName) {
//        UUID uuid = Utilities.getPlayerUUID(playerName);
//
//        if (uuid == null) return MiniMessage.get().parse(Config.Messages.PLAYER_NOT_FOUND.getMessage().replaceAll("%player%", playerName));
//
//        return getOfflineSeen(uuid);
//    }
//
//    private static Component getOfflineSeen(UUID uuid) {
//        if (uuid == null) return Component.empty();
//
//        PlaytimeSeen lastSeen = Queries.getLastSeen(uuid);
//
//        if (lastSeen == null || lastSeen.getLastSeen() == 0) return MiniMessage.get().parse(Config.Messages.SEEN_TIME_NULL.getMessage());
//
//        return MiniMessage.get().parse(Config.Messages.SEEN_FORMAT.getMessage()
//                .replaceAll("%player%", Utilities.getPlayerName(uuid))
//                .replaceAll("%online/offline%", Config.Messages.SEEN_OFFLINE_FORMAT.getMessage())
//                .replaceAll("%time%", getPassedTime(lastSeen.getLastSeen()))
//                .replaceAll("%server%", lastSeen.getServer()));
//    }
}
