package com.alttd.commandManager.commands;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.database.queries.QueriesEconomy;
import com.alttd.database.queries.QueriesUserDiscordId;
import com.alttd.util.Util;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.List;
import java.util.UUID;

public class CommandBal extends DiscordCommand {

    private final CommandData commandData;

    public CommandBal() {
        commandData = Commands.slash(getName(), "Get your balance")
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
    }

    @Override
    public String getName() {
        return "bal";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long userId = event.getInteraction().getUser().getIdLong();
        ReplyCallbackAction replyCallbackAction = event.deferReply(true);
        QueriesUserDiscordId.getUUIDById(userId).thenAcceptAsync(optionalUUID -> {
            if (optionalUUID.isEmpty()) {
                replyCallbackAction.setEmbeds(Util.genericErrorEmbed("Error", "Unable to find your minecraft account.")).queue();
                return;
            }
            UUID uuid = optionalUUID.get();

            QueriesEconomy.getBalance(uuid).thenAcceptAsync(optionalBalance -> {
                if (optionalBalance.isEmpty()) {
                    replyCallbackAction.setEmbeds(Util.genericErrorEmbed("Error", "Unable to find a balance for your minecraft account.")).queue();
                    return;
                }
                String formattedBalance = Util.formatNumber(optionalBalance.get());
                replyCallbackAction.setEmbeds(Util.genericSuccessEmbed("Balance", "Your balance is: $" + formattedBalance)).queue();
            });
        });
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(List.of()).queue();
    }

    @Override
    public String getHelpMessage() {
        return "This command will show you your in game balance";
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }
}
