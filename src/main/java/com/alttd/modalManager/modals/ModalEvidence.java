package com.alttd.modalManager.modals;

import com.alttd.database.queries.commandOutputChannels.CommandOutputChannels;
import com.alttd.database.queries.commandOutputChannels.OutputType;
import com.alttd.modalManager.DiscordModal;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.util.List;

public class ModalEvidence extends DiscordModal {

    @Override
    public String getModalId() {
        return "evidence";
    }

    @Override
    public void execute(ModalInteractionEvent event) {
        List<ModalMapping> modalMappings = event.getValues();
        if (modalMappings.size() != 4) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Found the wrong number of fields in your form input"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        String user = modalMappings.get(0).getAsString();
        String punishmentType = modalMappings.get(1).getAsString();
        String reason = modalMappings.get(2).getAsString();
        String evidence = modalMappings.get(3).getAsString();

        Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Couldn't find this guild"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        GuildMessageChannel channel = guild.getChannelById(GuildMessageChannel.class, CommandOutputChannels.getOutputChannel(guild.getIdLong(), OutputType.EVIDENCE));
        if (channel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This guild does not have a suggestion review channel or it's not the right channel type"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        Member member = event.getMember();
        if (member == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This command can only be executed from a guild"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        MessageEmbed evidenceEmbed = new EmbedBuilder()
                .setAuthor(member.getEffectiveName(), null, member.getAvatarUrl())
                .setTitle("Evidence by " + member.getEffectiveName())
                .addField("`" + user + "`", "", false)
                .addField(punishmentType, reason, false)
                .setDescription(evidence)
                .setFooter(member.getIdLong() + "")
                .build();

        ReplyCallbackAction replyCallbackAction = event.deferReply(true);
        channel.sendMessageEmbeds(evidenceEmbed)
                .queue(success -> replyCallbackAction.setEmbeds(Util.genericSuccessEmbed("Success", "Your evidence was submitted to the evidence channel!"), evidenceEmbed),
                        Util::handleFailure);
    }

    @Override
    public Modal getModal() {
        TextInput user = TextInput.create("user", "User", TextInputStyle.SHORT)
                .setPlaceholder("username/id")
                .setMinLength(1)
                .setRequired(true)
                .build();

        TextInput punishmentType = TextInput.create("punishment-type", "Punishment Type", TextInputStyle.SHORT)
                .setPlaceholder("punishment type")
                .setMinLength(3)
                .setRequired(true)
                .build();

        TextInput reason = TextInput.create("reason", "Reason", TextInputStyle.SHORT)
                .setPlaceholder("punishment reason")
                .setMinLength(10)
                .setRequired(true)
                .build();

        TextInput evidence = TextInput.create("evidence", "Evidence", TextInputStyle.PARAGRAPH)
                .setPlaceholder("evidence")
                .setRequiredRange(10, 1000)
                .setRequired(true)
                .build();

        return Modal.create(getModalId(), "Evidence")
                .addActionRows(ActionRow.of(user), ActionRow.of(punishmentType), ActionRow.of(reason), ActionRow.of(evidence))
                .build();
    }
}
