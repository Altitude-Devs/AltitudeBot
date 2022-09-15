package com.alttd.buttonManager.buttons.suggestionReview;

import com.alttd.buttonManager.DiscordButton;
import com.alttd.database.queries.commandOutputChannels.CommandOutputChannels;
import com.alttd.database.queries.commandOutputChannels.OutputType;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;

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
        long suggestionChannelId = CommandOutputChannels.getOutputChannel(message.getGuild().getIdLong(), OutputType.SUGGESTION);
        long modLogChannelId = CommandOutputChannels.getOutputChannel(message.getGuild().getIdLong(), OutputType.MOD_LOG);

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

        GuildMessageChannel suggestionChannel = guild.getChannelById(GuildMessageChannel.class, suggestionChannelId);
        if (suggestionChannel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This server does not have a valid suggestion channel"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        GuildMessageChannel modLogChannel = guild.getChannelById(GuildMessageChannel.class, modLogChannelId);
        if (modLogChannel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This server does not have a valid suggestion channel"))
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
                .setColor(Color.GRAY)
                .setTitle(fields.get(0).getName())
                .setDescription(fields.get(0).getValue())
                .build();
        suggestionChannel.sendMessageEmbeds(suggestionMessage).queue(success -> {
            message.delete().queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            event.replyEmbeds(Util.genericSuccessEmbed("Success", "The suggestion was accepted and posted in the suggestion channel")).setEphemeral(true).queue();
            modLogChannel.sendMessageEmbeds(new EmbedBuilder(suggestionMessage).addField("Accepted", event.getUser().getAsMention(), false).setColor(Color.GREEN).build())
                    .queue(RestAction.getDefaultSuccess(), Util::handleFailure);
        }, failure -> event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to send suggestion to the suggestion channel"))
                .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure));
    }

    @Override
    public Button getButton() {
        return Button.primary(getButtonId(), "Accept Suggestion");
    }
}
