package com.alttd.modalManager.modals;

import com.alttd.database.queries.commandOutputChannels.CommandOutputChannels;
import com.alttd.database.queries.commandOutputChannels.OutputType;
import com.alttd.modalManager.DiscordModal;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;

public class ModalCrateItem extends DiscordModal {
    @Override
    public String getModalId() {
        return "crate_item";
    }

    @Override
    public void execute(ModalInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();
        if (member == null || guild == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This command can only be used from within a guild."))
                    .setEphemeral(true).queue();
            return;
        }

        GuildChannel outputChannel = CommandOutputChannels.getOutputChannel(guild, OutputType.CRATE_TEAM);
        if (outputChannel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This guild does not have a crate team channel or it's not the right channel type"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        if (!(outputChannel instanceof  GuildMessageChannel channel)) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", outputChannel.getType().name() + " is not a valid crate team channel type"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        String item = getValidString(event.getValue("item"), event, true);
        if (item == null)
            return;

        String itemName = getValidString(event.getValue("item_name"), event, true);
        if (itemName == null)
            return;

        String lore = getValidString(event.getValue("lore"), event, true);
        if (lore == null)
            return;

        String enchants = getValidString(event.getValue("enchants"), event, false);
        String explanation = getValidString(event.getValue("explanation"), event, false);

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Crate Item Suggestion")
                .setColor(Color.ORANGE)
                .setAuthor(member.getEffectiveName(), member.getAvatarUrl())
                .addField("Item", item, false)
                .addField("Item Name", itemName, false)
                .addField("Lore", lore, false);
        if (enchants != null)
            embedBuilder.addField("Enchants", enchants, false);
        if (explanation != null)
            embedBuilder.addField("Explanation", explanation, false);
        MessageEmbed itemSuggestionEmbed = embedBuilder.build();

        event.deferReply(true).queue(
                defer ->channel.sendMessageEmbeds(itemSuggestionEmbed).queue(
                        send -> defer.editOriginalEmbeds(Util.genericSuccessEmbed("Success", "Your suggestion has been submitted to the crate team!"),
                                itemSuggestionEmbed).queue(RestAction.getDefaultSuccess(), Util::handleFailure),
                        error -> defer.editOriginalEmbeds(Util.genericErrorEmbed("Error", "Unable to submit your suggestion to the crate team channel..."),
                                itemSuggestionEmbed).queue(RestAction.getDefaultSuccess(), Util::handleFailure)),
                Util::handleFailure);
    }

    @Override
    public Modal getModal() {
        TextInput item = TextInput.create("item", "Item", TextInputStyle.SHORT)
                .setPlaceholder("Bone")
                .setRequiredRange(1, 32)
                .setRequired(true)
                .build();

        TextInput itemName = TextInput.create("item_name", "Item Name", TextInputStyle.SHORT)
                .setPlaceholder("Scruff's Bone")
                .setRequiredRange(1, 32)
                .setRequired(true)
                .build();

        TextInput lore = TextInput.create("lore", "Lore", TextInputStyle.PARAGRAPH)
                .setPlaceholder("A bone owned by the Altitude Mascot")
                .setRequiredRange(1, 256)
                .setRequired(true)
                .build();

        TextInput enchants = TextInput.create("enchants", "Enchants", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Unbreaking 1")
                .setRequiredRange(1, 256)
                .setRequired(false)
                .build();

        TextInput explanation = TextInput.create("explanation", "The explanation behind your item", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Scruff loves strong bones")
                .setRequiredRange(1, 2000)
                .setRequired(false)
                .build();

        return Modal.create(getModalId(), "Crate Item Suggestion")
                .addActionRows(ActionRow.of(item), ActionRow.of(itemName), ActionRow.of(lore), ActionRow.of(enchants), ActionRow.of(explanation))
                .build();
    }

    public String getValidString(ModalMapping modalMapping, ModalInteractionEvent event, boolean required) {
        if (modalMapping == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Couldn't find modal"))
                    .setEphemeral(true).queue();
            return null;
        }

        String string = modalMapping.getAsString();
        if (string.isEmpty()) {
            if (required)
                event.replyEmbeds(Util.genericErrorEmbed("Error", "Couldn't find contents of modal"))
                        .setEphemeral(true).queue();
            return null;
        }

        return string;
    }
}
