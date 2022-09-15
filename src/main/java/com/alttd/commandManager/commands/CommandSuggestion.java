package com.alttd.commandManager.commands;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.config.MessagesConfig;
import com.alttd.modalManager.ModalManager;
import com.alttd.util.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.Collections;

public class CommandSuggestion extends DiscordCommand {

    private final CommandManager commandManager;
    private final CommandData commandData;
    private final ModalManager modalManager;

    public CommandSuggestion(JDA jda, ModalManager modalManager, CommandManager commandManager) {
        this.commandManager = commandManager;
        this.modalManager = modalManager;

        commandData = Commands.slash(getName(), "Open suggestion form.");
        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "suggestion";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Modal modal = modalManager.getModalFor("suggestion");
        if (modal == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error",
                    "Unable to find suggestion modal."))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        event.replyModal(modal).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(Collections.emptyList())
                .queue(RestAction.getDefaultSuccess(), Util::handleFailure);
    }

    @Override
    public String getHelpMessage() {
        return MessagesConfig.HELP_SUGGESTION;
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }
}
