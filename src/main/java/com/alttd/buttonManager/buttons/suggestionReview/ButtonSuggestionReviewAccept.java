package com.alttd.buttonManager.buttons.suggestionReview;

import com.alttd.buttonManager.DiscordButton;
import com.alttd.database.queries.commandOutputChannels.CommandOutputChannels;
import com.alttd.database.queries.commandOutputChannels.OutputType;
import com.alttd.util.Logger;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;
import java.util.List;

public class ButtonSuggestionReviewAccept extends DiscordButton {

    @Override
    public String getButtonId() {
        return "suggestion_review_accept";
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        Message message = event.getMessage();
        Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve guild"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        List<MessageEmbed> embeds = message.getEmbeds();
        if (embeds.size() != 1) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This message contains no embeds, can't be a suggestion"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        MessageEmbed reviewMessage = embeds.get(0);
        List<MessageEmbed.Field> fields = reviewMessage.getFields();
        if (fields.size() != 1) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This message's embed does not contain a field, can't be a suggestion"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        GuildChannel suggestionGuildChannel = CommandOutputChannels.getOutputChannel(message.getGuild(), OutputType.SUGGESTION);
        GuildChannel modLogGuildChannel = CommandOutputChannels.getOutputChannel(message.getGuild(), OutputType.MOD_LOG);

        TextChannel modLogChannel = validModLogChannel(event, modLogGuildChannel);
        if (modLogChannel == null)
            return;

        if (suggestionGuildChannel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This server does not have a valid suggestion channel."))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        String mentionMember = reviewMessage.getDescription();
        if (mentionMember == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This message contains no description, can't be a suggestion"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        MessageEmbed suggestionMessage = new EmbedBuilder(reviewMessage)
                .clearFields()
                .setColor(Color.GRAY)
                .setTitle(fields.get(0).getName())
                .setDescription(fields.get(0).getValue())
                .addField("Suggestion by", mentionMember, false)
                .build();

        if (suggestionGuildChannel instanceof ForumChannel forumChannel) {
            sendSuggestionInForum(forumChannel, modLogChannel, fields.get(0), suggestionMessage, mentionMember, event);
        } else if (suggestionGuildChannel instanceof TextChannel forumChannel) {
            sendSuggestionEmbed(forumChannel, modLogChannel, suggestionMessage, event);
        } else {
            event.replyEmbeds(Util.genericErrorEmbed("Error", suggestionGuildChannel.getType().name() + " is not a valid suggestion channel"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
        }
    }

    private TextChannel validModLogChannel(ButtonInteractionEvent event, GuildChannel guildChannel) {
        if (guildChannel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This server does not have a valid mod log channel"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return null;
        }

        if (!(guildChannel instanceof TextChannel channel)) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "A mod log channel can't be of type: " + guildChannel.getType().name()))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return null;
        }
        return channel;
    }

    public void sendSuggestionEmbed(TextChannel suggestionChannel, TextChannel modLog, MessageEmbed suggestionMessage, ButtonInteractionEvent event) {
        suggestionChannel.sendMessageEmbeds(suggestionMessage).queue(success -> {
            event.getMessage().delete().queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            event.replyEmbeds(Util.genericSuccessEmbed("Success", "The suggestion was accepted and posted in the suggestion channel")).setEphemeral(true).queue();
            sendModLog(modLog, suggestionMessage, event);
        }, failure -> event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to send suggestion to the suggestion channel"))
                .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure));
    }

    public void sendSuggestionInForum(ForumChannel forumChannel, TextChannel modLog, MessageEmbed.Field field, MessageEmbed suggestionMessage, String mentionMember, ButtonInteractionEvent event) {
        MessageCreateData messageCreateData = new MessageCreateBuilder().addContent("**Suggestion by: " + mentionMember + "**\n\n" + field.getValue() + "\u200B").build();

        if (field.getName() == null) {
            Logger.altitudeLogs.error("Encountered empty name field when sending suggestion in forum");
            return;
        }

        forumChannel.createForumPost(field.getName(), messageCreateData).queue(success -> {
            event.getMessage().delete().queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            event.replyEmbeds(Util.genericSuccessEmbed("Success", "The suggestion was accepted and posted in the suggestion channel")).setEphemeral(true).queue();
            sendModLog(modLog, suggestionMessage, event);
            success.getMessage().addReaction(Emoji.fromUnicode("\uD83D\uDC4D")).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            success.getMessage().addReaction(Emoji.fromUnicode("\uD83D\uDC4E")).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            forumChannel.getAvailableTags().stream()
                    .filter(forumTag -> {
                        EmojiUnion emoji = forumTag.getEmoji();
                        if (emoji == null)
                            return false;
                        return emoji.getAsReactionCode().equals("\uD83D\uDD27");
                    })
                    .findAny()
                    .ifPresentOrElse(forumTag -> success.getThreadChannel().getManager().setAppliedTags(ForumTagSnowflake.fromId(forumTag.getIdLong()))
                            .queue(RestAction.getDefaultSuccess(), Util::handleFailure), () -> {
                        Logger.altitudeLogs.warning("No [Unanswered] reaction found for suggestion");
                    });
        }, failure -> event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to send suggestion to the suggestion channel"))
                .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure));
    }

    public void sendModLog(TextChannel modLog, MessageEmbed suggestionMessage, ButtonInteractionEvent event) {
        modLog.sendMessageEmbeds(new EmbedBuilder(suggestionMessage).addField("Accepted", event.getUser().getAsMention(), false).setColor(Color.GREEN).build())
                .queue(RestAction.getDefaultSuccess(), Util::handleFailure);
    }

    @Override
    public Button getButton() {
        return Button.success(getButtonId(), "Accept Suggestion");
    }
}
