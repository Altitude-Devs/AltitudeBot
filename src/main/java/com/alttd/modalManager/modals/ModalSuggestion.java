package com.alttd.modalManager.modals;

import com.alttd.buttonManager.ButtonManager;
import com.alttd.database.queries.commandOutputChannels.CommandOutputChannels;
import com.alttd.database.queries.commandOutputChannels.OutputType;
import com.alttd.modalManager.DiscordModal;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.List;

public class ModalSuggestion extends DiscordModal {

    private final ButtonManager buttonManager;

    public ModalSuggestion(ButtonManager buttonManager) {
        this.buttonManager = buttonManager;
    }

    @Override
    public String getModalId() {
        return "suggestion";
    }

    @Override
    public void execute(ModalInteractionEvent event) {
        List<ModalMapping> modalMappings = event.getValues();
        if (modalMappings.size() != 2) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Found the wrong number of fields in your form input"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        String title = modalMappings.get(0).getAsString().replaceAll("\u200B", "");
        String desc = modalMappings.get(1).getAsString().replaceAll("\u200B", "");

        Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Couldn't find this guild"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        GuildChannel outputChannel = CommandOutputChannels.getOutputChannel(guild, OutputType.SUGGESTION_REVIEW);
        if (outputChannel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This guild does not have a suggestion review channel or it's not the right channel type"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        if (!(outputChannel instanceof  GuildMessageChannel channel)) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", outputChannel.getType().name() + " is not a valid suggestion review channel type"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        Member member = event.getMember();
        if (member == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This command should only be executed from a guild"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        MessageEmbed suggestionToPlayer = new EmbedBuilder().setTitle("Your suggestion").addField(title, desc, false).build();
        MessageEmbed reviewMessage = new EmbedBuilder()
                .setTitle("New suggestion")
                .appendDescription(member.getAsMention())
                .addField(title, desc, false)
                .setAuthor(member.getEffectiveName(), null, member.getAvatarUrl())
                .setFooter(member.getIdLong() + "")
                .build();

        channel.sendMessageEmbeds(reviewMessage).queue(
                success -> addButtons(success, event.deferReply(true), suggestionToPlayer),
                failure -> event.replyEmbeds(Util.genericErrorEmbed("Error", "Couldn't submit suggestion for review."), suggestionToPlayer)
                        .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure));
    }

    public void addButtons(Message message, ReplyCallbackAction replyCallbackAction, MessageEmbed suggestionToPlayer) {
        Button suggestionReviewAccept = buttonManager.getButtonFor("suggestion_review_accept");
        Button suggestionReviewDeny = buttonManager.getButtonFor("suggestion_review_deny");
        if (suggestionReviewAccept == null || suggestionReviewDeny == null) {
            replyCallbackAction.setEmbeds(Util.genericErrorEmbed("Error", "Unable to prepare your suggestion for review."))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        message.editMessageComponents().setActionRow(suggestionReviewAccept, suggestionReviewDeny).queue(
                success -> replyCallbackAction.setEmbeds(Util.genericSuccessEmbed("Success", "Your suggestion was submitted for review!"), suggestionToPlayer)
                        .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure),
                failure -> replyCallbackAction.setEmbeds(Util.genericErrorEmbed("Error", "Couldn't prepare your suggestion for review."), suggestionToPlayer)
                        .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure));
    }

    @Override
    public Modal getModal() {
        TextInput title = TextInput.create("title", "Title", TextInputStyle.SHORT)
                .setPlaceholder("You suggestion in one sentence")
                .setRequiredRange(10, 100)
                .setRequired(true)
                .build();

        TextInput body = TextInput.create("body", "Body", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Suggestion...")
                .setRequiredRange(30, 1024)
                .setRequired(true)
                .build();

        return Modal.create(getModalId(), "Suggestion Form")
                .addActionRows(ActionRow.of(title), ActionRow.of(body))
                .build();
    }
}
