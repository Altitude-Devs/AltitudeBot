package com.alttd.contextMenuManager.contextMenus;

import com.alttd.contextMenuManager.DiscordContextMenu;
import com.alttd.util.Kanboard;
import com.alttd.util.Util;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.concurrent.CompletableFuture;

public class ContextMenuForwardToKanboard extends DiscordContextMenu {
    @Override
    public String getContextMenuId() {
        return "Forward To Kanboard";
    }

    @Override
    public void execute(UserContextInteractionEvent event) {
        event.getInteraction().replyEmbeds(Util.genericErrorEmbed("Error", "This interaction should have been a message interaction"))
                .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
    }

    @Override
    public void execute(MessageContextInteractionEvent event) {
        if (!event.isFromGuild()) {
            return;
        }
        Message message = event.getInteraction().getTarget();

        CompletableFuture<Boolean> booleanCompletableFuture = Kanboard.forwardMessageToKanboard(message);
        event.deferReply(true).queue(defer -> booleanCompletableFuture.thenAcceptAsync(result -> {
            if (!result) {
                defer.editOriginalEmbeds(Util.genericErrorEmbed("Error", "Unable to forward message to Kanboard")).queue();
                return;
            }
            defer.editOriginalEmbeds(Util.genericSuccessEmbed("Success", "Forwarded message to Kanboard board!")).queue();
        }));
    }

    @Override
    public CommandData getUserContextInteraction() {
        return Commands.message(getContextMenuId())
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
}
