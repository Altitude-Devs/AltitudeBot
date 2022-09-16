package com.alttd.request;

import com.alttd.AltitudeBot;
import com.alttd.util.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import java.awt.*;

public class RequestManager {

    public static void init() {
        RequestConfig.reload();
        if (RequestConfig.REQUEST_MESSAGE == null || RequestConfig.REQUEST_MESSAGE.isEmpty())
            sendRequestMessage();
    }

    public static Pair<EmbedBuilder, SelectMenu.Builder> getRequestEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        SelectMenu.Builder selectMenuBuilder = SelectMenu.create("request:create");
        embedBuilder.setDescription("Select an option below to open a request!\n")
                .setTitle("Create a new request.")
                .setColor(new Color(41, 43, 47));

        for (Request request : RequestConfig.requests) {
            embedBuilder.addField(request.getName(), request.getDescription(), false);
            selectMenuBuilder.addOption(request.getName(), "request:open:" + request.getId(), request.getDescription(), null);
        }

        return new Pair<>(embedBuilder, selectMenuBuilder);
    }

    public static void sendRequestMessage() {
        TextChannel channel = AltitudeBot.getInstance().getJDA().getGuildById(RequestConfig.REQUEST_GUILD_ID).getTextChannelById(RequestConfig.REQUEST_CHANNEL);
        Pair<EmbedBuilder, SelectMenu.Builder> pair = getRequestEmbed();
        channel.sendMessageEmbeds(pair.getValue0().build()).setActionRow(
                pair.getValue1().build()
        ).queue(m -> RequestConfig.setRequestMessage(m.getId()));
    }

    public static void updateRequestMessage() {
        TextChannel channel = AltitudeBot.getInstance().getJDA().getGuildById(RequestConfig.REQUEST_GUILD_ID).getTextChannelById(RequestConfig.REQUEST_CHANNEL);
        Pair<EmbedBuilder, SelectMenu.Builder> pair = getRequestEmbed();
        channel.editMessageEmbedsById(RequestConfig.REQUEST_MESSAGE, pair.getValue0().build())
                .setActionRow(
                        pair.getValue1().build()
                ).queue(m -> RequestConfig.setRequestMessage(m.getId()));
    }

    public static Request getRequestById(String id) {
        return RequestConfig.requests.stream().filter(request -> request.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public static void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        String[] actions = event.getComponentId().split(":");
        if (actions[1].equals("create")) {
            String[] selection = event.getSelectedOptions().get(0).getValue().split(":");
            if (selection[0].equals("request") && selection[1].equals("open")) {
                String id = selection[2];
                event.replyModal(getRequestById(id).modal(event.getMember())).queue();
                updateRequestMessage(); // You can't use a select menu option twice in a row, updating it fixes that.
            }
        }
    }

    public static void onModalInteractionEvent(ModalInteractionEvent event) {
        String s = event.getModalId();
        String[] strings = s.split(":", 2);
        getRequestById(strings[1]).createThread(event.getMember(), event.getValue("title").getAsString(), event.getValue("request").getAsString());
        event.reply("Thanks for your request!").setEphemeral(true).queue();
    }

    public static void onButtonInteractionEvent(ButtonInteractionEvent event) {
        String s = event.getComponentId();
        String[] strings = s.split(":", 5);
        String requestId = strings[1];
        String threadId = strings[2];
        String messageId = strings[3];
        String type = strings[4]; // progress, complete, denied

        // TODO update the stored request in the database
        switch (type) {
            case "denied" -> {
                // TODO open a new modal to input a reason?
                // could also do this by command?
                event.reply("This request has been denied by " + event.getMember().getAsMention()).queue();
                ThreadChannel threadChannel = AltitudeBot.getInstance().getJDA().getGuildById(RequestConfig.REQUEST_GUILD_ID).getThreadChannelById(threadId);
                threadChannel.getManager().setArchived(true).setLocked(true).queue();
            }
            case "complete" -> {
                // TODO open a new modal to input a reason?
                // could also do this by command?
                event.reply("This request has been completed by " + event.getMember().getAsMention()).queue();
                ThreadChannel threadChannel = AltitudeBot.getInstance().getJDA().getGuildById(RequestConfig.REQUEST_GUILD_ID).getThreadChannelById(threadId);
                threadChannel.getManager().setArchived(true).setLocked(true).queue();
            }
            case "progress" -> {
                // TODO open a new modal to input a reason?
                // edit the message to show who is working on it?
            }
        }

    }

}
