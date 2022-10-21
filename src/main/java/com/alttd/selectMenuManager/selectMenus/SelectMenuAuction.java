package com.alttd.selectMenuManager.selectMenus;

import com.alttd.database.queries.QueriesAuctions.Auction;
import com.alttd.database.queries.QueriesAuctions.QueriesAuction;
import com.alttd.schedulers.AuctionScheduler;
import com.alttd.selectMenuManager.DiscordSelectMenu;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SelectMenuAuction extends DiscordSelectMenu {
    @Override
    public String getSelectMenuId() {
        return "auction";
    }

    @Override
    public void execute(SelectMenuInteractionEvent event) {
        Member member = event.getMember();
        if (member == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This can only be used in a guild"))
                    .setEphemeral(true).queue();
            return;
        }
        AuctionScheduler auctionScheduler = AuctionScheduler.getInstance();
        if (auctionScheduler == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to get scheduler"))
                    .setEphemeral(true).queue();
            return;
        }
        Auction auction = auctionScheduler.getAuction(event.getMessage().getIdLong());
        if (auction == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to find this auction in the database, please try again"))
                    .setEphemeral(true).queue();
            return;
        }

        List<SelectOption> collect = event.getInteraction().getSelectedOptions().stream().filter(opt -> !opt.isDefault()).collect(Collectors.toList());
        if (collect.isEmpty()) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Received default input"))
                    .setEphemeral(true).queue();
            return;
        }
        if (collect.size() != 1) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Received invalid number of inputs, can only handle one"))
                    .setEphemeral(true).queue();
            return;
        }

        SelectOption selectOption = collect.get(0);
        String value = selectOption.getValue();
        int bid;
        try {
            bid = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Received invalid input"))
                    .setEphemeral(true).queue();
            return;
        }

        MessageEmbed messageEmbed = getMessageEmbed(event.getMessage().getEmbeds(), event);
        if (messageEmbed == null)
            return;
        if (messageEmbed.getAuthor() != null && messageEmbed.getAuthor().getName() != null && messageEmbed.getAuthor().getName().equalsIgnoreCase(member.getEffectiveName())) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "You own this auction so you can not bid on it."))
                    .setEphemeral(true).queue();
            //TODO store auction owner so this can be done better
            return;
        }
        BidFieldInfo bidFieldInfo = getPreviousBid(messageEmbed, event);
        if (bidFieldInfo == null)
            return;
        int prevBid;
        if (bidFieldInfo.bid() == 0)
            prevBid = auction.getStartingPrice();
        else
            prevBid = bidFieldInfo.bid();

        int currentBid = prevBid + bid;
        if (selectOption.getLabel().startsWith("Insta Buy")) {
            auctionScheduler.finishAuction(auction, member);
            event.replyEmbeds(Util.genericSuccessEmbed("Success", "You successfully insta bought the item for " + bid + "!"))
                    .setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder(messageEmbed)
                .clearFields()
                .setTimestamp(Instant.now());
        if (auction.updateExpiry()) {
            auctionScheduler.updateAuction(auction);
            String description = messageEmbed.getDescription();
            if (description != null)
                embedBuilder.setDescription(description.substring(0, description.lastIndexOf("Closes <t:")))
                        .appendDescription("Closes <t:" + TimeUnit.MILLISECONDS.toSeconds(auction.getExpireTime()) + ":R>");
        }
        if (bidFieldInfo.member() != null)
            embedBuilder.addField("Previous Bid", "$" + bidFieldInfo.bid() + " by <@" + bidFieldInfo.member() + ">", false);
        embedBuilder.addField("Current Bid", "$" + currentBid + " by " + event.getMember().getAsMention(), false);
        ReplyCallbackAction replyCallbackAction = event.deferReply(true);

        int finalPrevBid = prevBid;
        event.getMessage().editMessageEmbeds(embedBuilder.build()).queue(
                success -> {
                    if (auction.updateExpiry())
                        auctionScheduler.updateAuction(auction);
                    replyCallbackAction.setEmbeds(Util.genericSuccessEmbed("Success", "You successfully increased the bid from " + finalPrevBid + " to " + currentBid + "!"))
                            .queue();
                },
                error -> replyCallbackAction.setEmbeds(Util.genericErrorEmbed("Error", "Unable to finish your bid")).queue());
    }

    /**
     * Expecting to find:
     * a: No fields
     * b: One field containing the current bid
     * c: Two fields, one containing the current bid and one for the previous bid
     * <p>
     * option a: return BidFieldInfo with the bid set to 0 and member to null
     * option b: check if field matches roughly this:
     *      Name: Current Bid
     *      Value: 160 by <@212303885988134914>
     *      if it does set the BidFieldInfo value to 160, and attempt ot get the user from the <@ string,
     *      set member to the member belonging to that user id  or null if we can't find it
     * option c: Find the field with the name Current Bid and proceed with that field as if it was option b
     * @param messageEmbed Embed to find the fields for
     * @param event Event that we need to respond to for errors
     * @return BidFieldInfo or null if there was an error
     */
    private BidFieldInfo getPreviousBid(MessageEmbed messageEmbed, SelectMenuInteractionEvent event) {
        List<MessageEmbed.Field> fields = messageEmbed.getFields();
        if (fields.size() > 2) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This auction embed has the wrong number of fields"))
                    .setEphemeral(true).queue();
            return null;
        }

        if (fields.isEmpty())
            return new BidFieldInfo(0, null);

        MessageEmbed.Field field = fields.get(0);
        if (field.getName() == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Found field with no name"))
                    .setEphemeral(true).queue();
            return null;
        }

        if (!field.getName().equals("Current Bid")) {
            if (fields.size() != 2) {
                event.replyEmbeds(Util.genericErrorEmbed("Error", "Found one field and it's not the right one"))
                        .setEphemeral(true).queue();
                return null;
            }
            field = fields.get(1);
            if (field.getName() == null || !field.getName().equals("Current Bid")) {
                event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to find current bid but it should be there"))
                        .setEphemeral(true).queue();
                return null;
            }
        }
        String value1 = field.getValue();
        if (value1 == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Received default input"))
                    .setEphemeral(true).queue();
            return null;
        }
        String[] s = value1.split(" ");
        if (s.length < 3) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Field has incorrect input"))
                    .setEphemeral(true).queue();
            return null;
        }

        int bid;
        try {
            bid =  Integer.parseInt(s[0].substring(1));
        } catch (NumberFormatException e) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Field had the wrong value"))
                    .setEphemeral(true).queue();
            return null;
        }

        String substring = s[2].substring(2, s[2].length() - 1);
        try {
            long l = Long.parseLong(substring);

            return new BidFieldInfo(bid, l);
        } catch (NumberFormatException e) {
            return new BidFieldInfo(bid, null);
        }
    }

    private MessageEmbed getMessageEmbed(List<MessageEmbed> embeds, SelectMenuInteractionEvent event) {
        if (embeds.isEmpty()) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Input came from message with no embeds"))
                    .setEphemeral(true).queue();
            return null;
        }
        return embeds.get(0);
    }

    @Override
    public SelectMenu getSelectMenu(SelectOption... selectOptions) {
        SelectMenu.Builder builder = SelectMenu.create(getSelectMenuId());
        if (selectOptions != null)
            builder.addOptions(selectOptions);
        return builder.build();
    }
}

record BidFieldInfo(int bid, Long member) {}
