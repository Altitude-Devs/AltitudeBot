package com.alttd.buttonManager.buttons.suggestionReview;

import com.alttd.buttonManager.DiscordButton;
import com.alttd.database.queries.commandOutputChannels.CommandOutputChannels;
import com.alttd.database.queries.commandOutputChannels.OutputType;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;
import java.util.List;

public class ButtonSuggestionReviewDeny extends DiscordButton {

    @Override
    public String getButtonId() {
        return "suggestion_review_deny";
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        Message message = event.getMessage();
        GuildChannel guildChannel = CommandOutputChannels.getOutputChannel(message.getGuild(), OutputType.MOD_LOG);
        TextChannel channel = validModLogChannel(event, guildChannel);
        if (channel == null)
            return;

        List<MessageEmbed> embeds = message.getEmbeds();
        if (embeds.size() != 1) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This message contains no embeds, can't be a suggestion"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve guild"))
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

        MessageEmbed suggestionMessage = new EmbedBuilder(reviewMessage)
                .clearFields()
                .setColor(Color.RED)
                .setTitle(fields.get(0).getName())
                .setDescription(fields.get(0).getValue())
                .build();
        channel.sendMessageEmbeds(suggestionMessage).queue(success -> {
            message.delete().queue();
            event.replyEmbeds(Util.genericSuccessEmbed("Success", "The suggestion was denied and logged"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
        }, failure -> {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to send suggestion to the suggestion channel"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
        });
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

    @Override
    public Button getButton() {
        return Button.danger(getButtonId(), "Deny Suggestion");
    }
}
