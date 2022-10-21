package com.alttd.schedulers;

import com.alttd.database.queries.QueriesAuctions.Auction;
import com.alttd.database.queries.QueriesAuctions.QueriesAuction;
import com.alttd.database.queries.commandOutputChannels.CommandOutputChannels;
import com.alttd.database.queries.commandOutputChannels.OutputType;
import com.alttd.util.Logger;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionScheduler {
    private static AuctionScheduler instance = null;
    private final HashMap<Long, Auction> auctions;
    private Auction nextAuction;

    private AuctionScheduler() {
        instance = this;
        auctions = QueriesAuction.getAuctions();
        if (auctions == null) {
            Logger.severe("Unable to retrieve auctions");
            instance = null;
            return;
        }
        setNextAuction();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(new AuctionScheduler.AuctionRun(), 0, 1, TimeUnit.SECONDS);
    }

    private void setNextAuction() {
        Optional<Auction> first = auctions.values().stream().sorted().findFirst();
        if (first.isEmpty())
            nextAuction = null;
        else
            nextAuction = first.get();
    }

    public static AuctionScheduler getInstance() {
        if (instance == null)
            instance = new AuctionScheduler();
        return instance;
    }

    public synchronized void addAuction(Auction auction) {
        if (!QueriesAuction.saveAuction(auction))
            Logger.warning("Unable to save auction %", auction.getMessageId() + "");
        auctions.put(auction.getMessageId(), auction);
        setNextAuction();
    }

    public synchronized void updateAuction(Auction auction) {
        QueriesAuction.saveAuction(auction);
        setNextAuction();
    }

    public synchronized void removeAuction(Auction auction) {
        auctions.remove(auction.getMessageId());
        setNextAuction();
        if (!QueriesAuction.removeAuction(auction))
            Logger.warning("Unable to remove auction %", auction.getMessageId() + "");
    }

    public Auction getAuction(long messageId) {
        return auctions.getOrDefault(messageId, null);
    }

    public synchronized void finishAuction(Auction auction, @Nullable Member instaBuy) {
        auction.updateMessage(success -> {
            List<MessageEmbed> embeds = success.getEmbeds();
            if (embeds.isEmpty()) {
                Logger.warning("Received auction with no embed contents");
                return;
            }
            GuildChannel outputChannel = CommandOutputChannels.getOutputChannel(success.getGuild(), OutputType.AUCTION_LOG);
            if (outputChannel != null) {
                if (!(outputChannel instanceof  GuildMessageChannel channel)) {
                    Logger.warning("Error" + outputChannel.getType().name() + " is not a valid crate auction log channel type");
                    return;
                }
                if (!channel.canTalk()) {
                    Logger.warning("Error can't talk in auction log channel");
                    return;
                }
                if (sendEmbed(embeds.get(0), channel, instaBuy))
                    success.delete().queue();
            } else
                success.delete().queue();
        }, Logger::warning);
        removeAuction(auction);
    }

    private boolean sendEmbed(MessageEmbed embed, GuildMessageChannel textChannel, Member instaBuy) {
        EmbedBuilder embedBuilder = new EmbedBuilder(embed)
                .clearFields();
        List<MessageEmbed.Field> fields = embed.getFields();
        if (instaBuy != null)
            embedBuilder.addField("Winning Bid", "Insta bought by " + instaBuy.getAsMention(), false);
        else if (!fields.isEmpty()) {
            MessageEmbed.Field field = fields.get(0);
            if (field.getName() != null && field.getName().equals("Current Bid") && field.getValue() != null) {
                embedBuilder.addField("Winning Bid", field.getValue(), false);
            } else if (fields.size() == 2) {
                field = fields.get(1);
                if (field.getName() != null && field.getName().equals("Current Bid") && field.getValue() != null)
                    embedBuilder.addField("Winning Bid", field.getValue(), false);
            }
        }
        if (embedBuilder.getFields().size() != 0)
            embedBuilder.setColor(Color.GREEN);
        else
            embedBuilder.setColor(Color.RED);
        textChannel.sendMessageEmbeds(embedBuilder.build()).queue(Util::ignoreSuccess, failure -> Logger.warning("Failed to log auction result"));
        return true;
    }
    private class AuctionRun implements Runnable {
        @Override
        public void run() {
            long time = new Date().getTime();
            while (nextAuction != null && time > nextAuction.getExpireTime()) {
                finishAuction(nextAuction, null);
            }
        }
    }
}
