package com.alttd.modalManager.modals;

import com.alttd.database.queries.commandOutputChannels.CommandOutputChannels;
import com.alttd.database.queries.commandOutputChannels.OutputType;
import com.alttd.modalManager.DiscordModal;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.util.List;

public class ModalSuggestion extends DiscordModal {
    @Override
    public String getModalId() {
        return "suggestion";
    }

    @Override
    public void execute(ModalInteractionEvent event) {
        List<ModalMapping> modalMappings = event.getValues();
        if (modalMappings.size() != 2) {
            event.replyEmbeds(Util.genericErrorEmbed("Error",
                    "Found the wrong number of fields in your form input")).setEphemeral(true).queue();
            return;
        }
        String title = modalMappings.get(0).getAsString();
        String desc = modalMappings.get(1).getAsString();

        Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Couldn't find this guild")).setEphemeral(true).queue();
            return;
        }
        GuildMessageChannel channel = guild.getChannelById(GuildMessageChannel.class, CommandOutputChannels.getOutputChannel(guild.getIdLong(), OutputType.SUGGESTION_REVIEW));
        if (channel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error",
                    "This guild does not have a suggestion review channel or it's not the right channel type")).setEphemeral(true).queue();
            return;
        }

        //TODO add user tag in description
        MessageEmbed reviewMessage = new EmbedBuilder().setTitle("New suggestion").appendDescription("USER").addField(title, desc, false).build();
        channel.sendMessageEmbeds(reviewMessage).queue(success -> success.addReaction(null /*TODO FIX EMOTE this should be buttons*/).queue(aa -> {
            MessageEmbed responseEmbed = new EmbedBuilder().setTitle("Your submitted suggestion").addField(title, desc, false).build();
            event.replyEmbeds(responseEmbed).setEphemeral(true).queue();
            //TODO this should have something incase it errors
        }), failure -> {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Couldn't submit suggestion for review")).setEphemeral(true).queue();
            //TODO include their suggestion in this error (you can send more than one embed)
        });


    }

    @Override
    public Modal getModal() {
        TextInput title = TextInput.create("title", "Title", TextInputStyle.SHORT)
                .setPlaceholder("You suggestion in one sentence")
                .setRequiredRange(10, 100)
                .setRequired(true)
                .build();

        TextInput body = TextInput.create("body", "Body", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Your concerns go here")
                .setRequiredRange(30, 1024)
                .setRequired(true)
                .build();

        return Modal.create(getModalId(), "Suggestion Form")
                .addActionRows(ActionRow.of(title), ActionRow.of(body))
                .build();
    }
}
