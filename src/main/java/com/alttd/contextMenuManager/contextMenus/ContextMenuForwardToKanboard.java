package com.alttd.contextMenuManager.contextMenus;

import com.alttd.contextMenuManager.DiscordContextMenu;
import com.alttd.util.Kanboard;
import com.alttd.util.Util;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

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
        Member member = message.getMember();
        if (member == null)
            return;

        String title = "";
        if (member.getUser().equals(event.getJDA().getSelfUser())) {
            if (message.getContentRaw().startsWith("**Suggestion by:")) {
                if (message.getGuildChannel() instanceof ThreadChannel threadChannel) {
                    title = threadChannel.getName();
                }
            }
        }

        ReplyCallbackAction replyCallbackAction = event.deferReply(true);
        CompletableFuture<Boolean> booleanCompletableFuture;
        if (title.isBlank()) {
            booleanCompletableFuture = Kanboard.forwardMessageToKanboard(message);
        } else {
            booleanCompletableFuture = Kanboard.forwardMessageToKanboard(title, message.getContentDisplay());
        }
        booleanCompletableFuture.thenAcceptAsync(result -> {
            if (!result) {
                replyCallbackAction.setEmbeds(Util.genericErrorEmbed("Error", "Unable to forward message to Kanboard"));
                return;
            }
            replyCallbackAction.setEmbeds(Util.genericSuccessEmbed("Success", "Forwarded message to Kanboard board!"));
        });
    }

    @Override
    public CommandData getUserContextInteraction() {
        return Commands.message(getContextMenuId())
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
}
