package com.alttd.contextMenuManager.contextMenus;

import com.alttd.contextMenuManager.DiscordContextMenu;
import com.alttd.database.queries.commandOutputChannels.CommandOutputChannels;
import com.alttd.database.queries.commandOutputChannels.OutputType;
import com.alttd.modalManager.ModalManager;
import com.alttd.modalManager.modals.ModalReplySuggestion;
import com.alttd.util.Util;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.IThreadContainerUnion;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.RestAction;

public class ContextMenuRespondSuggestion extends DiscordContextMenu {

    private final ModalManager modalManager;

    public ContextMenuRespondSuggestion(ModalManager modalManager) {
        this.modalManager = modalManager;
    }

    @Override
    public String getContextMenuId() {
        return "Respond To Suggestion";
    }

    @Override
    public void execute(UserContextInteractionEvent event) {
        event.getInteraction().replyEmbeds(Util.genericErrorEmbed("Error", "This interaction should have been a message interaction"))
                .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
    }

    @Override
    public void execute(MessageContextInteractionEvent event) {
        Message message = event.getInteraction().getTarget();
        if (!isSuggestion(message)) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This is not a suggestion"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        Modal replySuggestion = modalManager.getModalFor("reply_suggestion");
        if (replySuggestion == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to find reply suggestion modal"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        ModalReplySuggestion.putMessage(event.getUser().getIdLong(), message); //TODO find a better way to do this
        event.replyModal(replySuggestion).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
    }

    @Override
    public CommandData getUserContextInteraction() {
        return Commands.message(getContextMenuId())
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    public boolean isSuggestion(Message message) {
        GuildChannel channel = CommandOutputChannels.getOutputChannel(message.getGuild(), OutputType.SUGGESTION);
        if (channel == null)
            return false;
        MessageChannelUnion messageChannel = message.getChannel();
        if (channel.getType().equals(ChannelType.FORUM)) {
            if (messageChannel.getType() != ChannelType.GUILD_PUBLIC_THREAD) {
                return false;
            }
            ThreadChannel threadChannel = messageChannel.asThreadChannel();
            IThreadContainerUnion parentChannel = threadChannel.getParentChannel();
            if (!parentChannel.getType().equals(ChannelType.FORUM))
                return false;

            return message.getIdLong() == messageChannel.getIdLong() && message.getAuthor().equals(message.getJDA().getSelfUser());
        } else {
            return channel.equals(messageChannel);
        }

    }
}
