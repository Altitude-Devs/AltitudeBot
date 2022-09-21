package com.alttd.commandManager.commands;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.util.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandUpdateCommands extends DiscordCommand {

    private final CommandData commandData;
    private final CommandManager commandManager;

    public CommandUpdateCommands(JDA jda, CommandManager commandManager) {
        this.commandManager = commandManager;
        this.commandData = Commands.slash(getName(), "Updates all commands for this bot in this guild")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .setGuildOnly(true);
        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "updatecommands";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This command can only be ran from a guild"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        ReplyCallbackAction replyCallbackAction = event.deferReply(true);
        List<CommandData> commandDataList = commandManager.getCommands(guild).stream().map(DiscordCommand::getCommandData).collect(Collectors.toList());
        guild.updateCommands().addCommands(commandDataList).queue(success -> replyCallbackAction.setEmbeds(Util.genericSuccessEmbed("Success", "Updated the commands in this guild"))
                .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure), Util::handleFailure);
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
