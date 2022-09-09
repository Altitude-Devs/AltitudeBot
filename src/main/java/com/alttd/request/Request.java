package com.alttd.request;

import com.alttd.AltitudeBot;
import com.alttd.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.restaction.ThreadChannelAction;

import java.awt.*;

@AllArgsConstructor
public class Request {

    @Getter
    private String id, category, channel, name, title, description, message;

    public Modal modal(Member member) {
        TextInput requestTitle = TextInput
                .create("title", title, TextInputStyle.PARAGRAPH)
                .build();

        TextInput requestMessage = TextInput
                .create("request", message.replaceAll("%member%", member.getEffectiveName()).subSequence(0, 45).toString(), TextInputStyle.PARAGRAPH)
                .build();

        return Modal.create("request:" + id, name)
                .addActionRow(requestTitle)
                .addActionRow(requestMessage)
                .build();
    }

    public void createThread(Member member, String title, String request) {
        TextChannel channel = AltitudeBot.getInstance().getJDA().getGuildById(RequestConfig.REQUEST_GUILD_ID).getTextChannelById(getChannel());
        ThreadChannelAction threadChannelAction = channel.createThreadChannel(title);
        threadChannelAction.queue(threadChannel -> {
            threadChannel.addThreadMember(member).queue();
            sendEmbed(threadChannel, title, request);
            channel.deleteMessageById(threadChannel.getId()).queue();
            // TODO store the request somewhere so it can be grabbed later
        });
    }

    public void sendEmbed(ThreadChannel channel, String title, String request) {
//        Pair<EmbedBuilder, ActionRow> pair = getRequestEmbed(channel.getId(), title, request);
        // pairs are not really possible here :(
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("title")
                .addField(getName(), request, false)
                .setColor(new Color(41, 43, 47));
        channel.sendMessageEmbeds(embedBuilder.build()).queue(message1 ->
                        channel.editMessageEmbedsById(message1.getId(), embedBuilder.build())
                        .setActionRow(
                                Button.primary("request:" + getId() + ":" + channel.getId() + ":" + message1.getId() + ":progress", "in progress"),
                                Button.success("request:" + getId() + ":" + channel.getId() + ":" + message1.getId() + ":complete", "complete"),
                                Button.danger("request:" + getId() + ":" + channel.getId() + ":" + message1.getId() + ":denied", "denied")
                        ).queue()
        );
    }

    public Pair<EmbedBuilder, ActionRow> getRequestEmbed(String channellId, String title, String request) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("title")
                .addField(getName(), request, false)
                .setColor(new Color(41, 43, 47));

        ActionRow actionRow = ActionRow.of(
                Button.primary("request:" + getId() + ":" + channellId + ":progress", "in progress"),
                Button.success("request:" + getId() + ":" + channellId + ":complete", "complete"),
                Button.danger("request:" + getId() + ":" + channellId + ":denied", "denied")
        );

        return new Pair<>(embedBuilder, actionRow);
    }

}
