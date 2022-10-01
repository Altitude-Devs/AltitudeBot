package com.alttd.commandManager.commands;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.modalManager.ModalManager;
import com.alttd.util.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.Collections;

public class CommandSuggestCrateItem extends DiscordCommand {

    private final CommandData commandData;
    private final ModalManager modalManager;

    public CommandSuggestCrateItem(JDA jda, ModalManager modalManager, CommandManager commandManager) {
        this.modalManager = modalManager;

        commandData = Commands.slash(getName(), "Open crate item suggestion form.")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "crateitemsuggestion";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Modal modal = modalManager.getModalFor("crate_item");
        if (modal == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error",
                            "Unable to find crate item suggestion modal."))
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
        return null;
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }
}
