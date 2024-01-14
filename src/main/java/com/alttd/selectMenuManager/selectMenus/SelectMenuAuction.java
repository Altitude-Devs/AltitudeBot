package com.alttd.selectMenuManager.selectMenus;

import com.alttd.database.queries.QueriesAuctionActions.AuctionAction;
import com.alttd.database.queries.QueriesAuctionActions.AuctionType;
import com.alttd.database.queries.QueriesAuctions.Auction;
import com.alttd.schedulers.AuctionScheduler;
import com.alttd.selectMenuManager.DiscordSelectMenu;
import com.alttd.selectMenuManager.SelectMenuManager;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SelectMenuAuction extends DiscordSelectMenu {

    private final SelectMenuManager selectMenuManager;

    public SelectMenuAuction(SelectMenuManager selectMenuManager) {
        this.selectMenuManager = selectMenuManager;
    }

    @Override
    public String getSelectMenuId() {
        return "auction";
    }

    @Override
    public void execute(StringSelectInteractionEvent event) {
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
        if (member.getIdLong() == auction.getUserId()) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "You own this auction so you can not bid on it."))
                    .setEphemeral(true).queue();
            return;
        }
        Instant now = Instant.now();
        if (selectOption.getLabel().startsWith("Insta Buy")) {
            Integer instaBuy = auction.getInstaBuy();
            auction.addAction(new AuctionAction(AuctionType.INSTA_BUY, member.getIdLong(), auction.getMessageId(), instaBuy == null ? -1 : instaBuy, now.toEpochMilli()));
            auctionScheduler.finishAuction(auction, member);
            event.replyEmbeds(Util.genericSuccessEmbed("Success", "You successfully insta bought the item for $" + Util.formatNumber(bid) + "!"))
                    .setEphemeral(true).queue();
            return;
        }

        AuctionAction lastAction = auction.getLastAction();
        int prevBid = (lastAction == null ? 0 : lastAction.price());
        int currentBid = prevBid + bid;

        if (selectOption.getLabel().startsWith("Starting Bid"))
            auction.addAction(new AuctionAction(AuctionType.STARTING_BID, member.getIdLong(), auction.getMessageId(), currentBid, now.toEpochMilli()));
        else
            auction.addAction(new AuctionAction(AuctionType.BID, member.getIdLong(), auction.getMessageId(), currentBid, now.toEpochMilli()));

        EmbedBuilder embedBuilder = new EmbedBuilder(messageEmbed)
                .clearFields()
                .setTimestamp(now);
        if (auction.updateExpiry()) {
            auctionScheduler.updateAuction(auction);
            String description = messageEmbed.getDescription();
            if (description != null)
                embedBuilder.setDescription(description.substring(0, description.lastIndexOf("Closes <t:")))
                        .appendDescription("Closes <t:" + TimeUnit.MILLISECONDS.toSeconds(auction.getExpireTime()) + ":R>");
        }
        if (lastAction != null)
            embedBuilder.addField("Previous Bid", "$" + Util.formatNumber(prevBid) + " by <@" + lastAction.userId() + ">", false);
        embedBuilder.addField("Current Bid", "$" + Util.formatNumber(currentBid) + " by " + event.getMember().getAsMention(), false);
        ReplyCallbackAction replyCallbackAction = event.deferReply(true);

        event.deferEdit().queue(edit -> edit.editOriginalEmbeds(embedBuilder.build()).queue(
                success -> {
                    if (auction.updateExpiry())
                        auctionScheduler.updateAuction(auction);
                    if (!selectOption.getLabel().startsWith("Starting Bid")) {
                        replyCallbackAction.setEmbeds(Util.genericSuccessEmbed("Success", "You successfully increased the bid from $" +
                                        Util.formatNumber(prevBid) + " to $" + Util.formatNumber(currentBid) + "!"))
                                .queue();
                        return;
                    }
                    replyCallbackAction.setEmbeds(Util.genericSuccessEmbed("Success", "You successfully made the first bid on this item ($" + Util.formatNumber(currentBid) + ")!"))
                            .queue();
                    success.editMessageComponents().setActionRow(auction.getSelectMenu(selectMenuManager, true)).queue();
                },
                error -> replyCallbackAction.setEmbeds(Util.genericErrorEmbed("Error", "Unable to finish your bid")).queue())
        );
    }

    private MessageEmbed getMessageEmbed(List<MessageEmbed> embeds, StringSelectInteractionEvent event) {
        if (embeds.isEmpty()) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Input came from message with no embeds"))
                    .setEphemeral(true).queue();
            return null;
        }
        return embeds.get(0);
    }

    @Override
    public SelectMenu getSelectMenu(List<SelectOption> selectOptions) {
        StringSelectMenu.Builder builder = StringSelectMenu.create(getSelectMenuId());
        if (selectOptions != null)
            builder.addOptions(selectOptions);
        return builder.build();
    }
}
