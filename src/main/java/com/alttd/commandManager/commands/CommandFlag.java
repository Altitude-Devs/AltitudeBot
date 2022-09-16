package com.alttd.commandManager.commands;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.database.queries.QueriesFlags.Flag;
import com.alttd.database.queries.QueriesFlags.QueriesFlags;
import com.alttd.database.queries.QueriesUserUUID;
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

public class CommandFlag extends DiscordCommand {

    private final CommandData commandData;

    public CommandFlag(JDA jda, CommandManager commandManager) {
        this.commandData = Commands.slash(getName(), "Show flags for a user")
                .addOption(OptionType.STRING, "user", "The user to show flags for", true)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);

        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "flaglist";
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
            flagListUUID(username, event);
        else
            flagListName(username, event);
    }

    private void flagListUUID(String username, SlashCommandInteractionEvent event) {
        UUID uuid;
        try {
            uuid = UUID.fromString(username);
        } catch (IllegalArgumentException e) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Invalid UUID"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        List<Flag> flags = QueriesFlags.getFlags(uuid);

        if (flags == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve flags for user"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        sendFlagEmbed(event, flags, uuid);
    }

    private void flagListName(String username, SlashCommandInteractionEvent event) {
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

        List<Flag> flags = QueriesFlags.getFlags(uuid);

        if (flags == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve flags for user"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        sendFlagEmbed(event, flags, username);
    }

    private void sendFlagEmbed(SlashCommandInteractionEvent event, List<Flag> flags, UUID uuid) {
        String username = QueriesUserUUID.getUsernameByUUID(uuid);
        if (username == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve username for uuid"))
                    .setEphemeral(true).queue();
            return;
        }
        sendFlagEmbed(event, flags, username);
    }

    private void sendFlagEmbed(SlashCommandInteractionEvent event, List<Flag> flags, String username) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Flaglist for: " + username);

        int i = 0;
        for (Flag flag : flags) {
            if (i == 24)
                break;
            embedBuilder.addField(getFlagTitle(flag), getFlagDescription(flag), false);
            i++;
        }
        event.replyEmbeds(embedBuilder.build())
                .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
    }

    private String getFlagTitle(Flag flag) {
        long days = TimeUnit.SECONDS.toDays(flag.length());
        long daysAgo = TimeUnit.MILLISECONDS.toDays(new Date().getTime() - TimeUnit.SECONDS.toMillis(flag.startTime()));
        return daysAgo + " days ago, for " + days + " days, by " + flag.flaggedBy();
    }

    private String getFlagDescription(Flag flag) {
        return "Reason: " + flag.reason() + "\n[" + ((new Date().after(new Date(TimeUnit.SECONDS.toMillis(flag.expireTime())))) ? "Expired" : "Active") + "]";
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(Collections.emptyList()).queue();
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
