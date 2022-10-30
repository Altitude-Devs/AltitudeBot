package com.alttd.database.queries.QueriesAuctions;

import com.alttd.AltitudeBot;
import com.alttd.database.queries.QueriesAuctionActions.AuctionAction;
import com.alttd.database.queries.QueriesAuctionActions.QueriesAuctionAction;
import com.alttd.selectMenuManager.DiscordSelectMenu;
import com.alttd.selectMenuManager.SelectMenuManager;
import com.alttd.util.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Auction implements Comparable {
    private final long userId, messageId, channelId, guildId;
    private final Integer instaBuy;
    private final int startingPrice, minimumIncrease;

    private long expireTime;

    private final LinkedList<AuctionAction> actions = new LinkedList<>();

    public Auction(long userId, long messageId, long channelId, long guildId, int startingPrice, long expireTime, int minimumIncrease, Integer instaBuy) {
        this.userId = userId;
        this.messageId = messageId;
        this.channelId = channelId;
        this.guildId = guildId;
        this.startingPrice = startingPrice;
        this.expireTime = expireTime;
        this.minimumIncrease = minimumIncrease;
        this.instaBuy = instaBuy;
    }

    public Auction(long userId, @NotNull Message message, long channelId, long guildId, int startingPrice, long expireTime, int minimumIncrease, Integer instaBuy) {
        this.userId = userId;
        this.messageId = message.getIdLong();
        this.channelId = channelId;
        this.guildId = guildId;
        this.startingPrice = startingPrice;
        this.expireTime = expireTime;
        this.minimumIncrease = minimumIncrease;
        this.instaBuy = instaBuy;
    }

    public void resetActions(LinkedList<AuctionAction> newAuctionActions) {
        actions.clear();
        actions.addAll(newAuctionActions);
        actions.sort(AuctionAction::compareTo);
    }

    public void addAction(AuctionAction auctionAction) {
        actions.add(auctionAction);
        QueriesAuctionAction.saveAuctionAction(auctionAction);
    }

    public AuctionAction getLastAction() {
        if (actions.size() == 0)
            return null;
        return actions.getLast();
    }

    public LinkedList<AuctionAction> getActions() {
        return actions;
    }

    public void updateMessage(Consumer<? super Message> success, @Nullable Consumer<? super String> failure) {
        Guild guild = AltitudeBot.getInstance().getJDA().getGuildById(guildId);
        if (guild == null) {
            if (failure != null)
                failure.accept("Unable to retrieve auction message due to invalid guild");
            return;
        }

        TextChannel textChannel = guild.getTextChannelById(channelId);
        if (textChannel == null) {
            if (failure != null)
                failure.accept("Unable to retrieve auction message due to invalid text channel");
            return;
        }

        textChannel.retrieveMessageById(messageId).queue(success, b -> {
            if (failure != null)
                failure.accept("Unable to retrieve the auction message");
        });
    }

    public long getUserId() {
        return userId;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public long getMessageId() {
        return messageId;
    }

    public long getChannelId() {
        return channelId;
    }

    public long getGuildId() {
        return guildId;
    }

    public int getStartingPrice() {
        return startingPrice;
    }

    public int getMinimumIncrease() {
        return minimumIncrease;
    }

    public Integer getInstaBuy() {
        return instaBuy;
    }

    public boolean updateExpiry() {
        long future = Instant.now().toEpochMilli() + TimeUnit.MINUTES.toMillis(5);
        if (expireTime < future) {
            expireTime = future;
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        Auction o1 = (Auction) o;
        return Long.compare(expireTime, o1.getExpireTime());
    }

    public SelectMenu getSelectMenu(SelectMenuManager selectMenuManager, boolean startingBidFinished) {
        DiscordSelectMenu discordAuction = selectMenuManager.getDiscordSelectMenuFor("auction");
        if (discordAuction == null)
            return null;
        int mediumIncrease = minimumIncrease * 5;
        List<SelectOption> selectOptionList = new ArrayList<>();

        if (startingBidFinished)
            selectOptionList.add(SelectOption.of("Increase bid by: $" + Util.formatNumber(minimumIncrease), "" + minimumIncrease));
        else
            selectOptionList.add(SelectOption.of("Starting Bid: $" + Util.formatNumber(startingPrice), "" + startingPrice));
        if (instaBuy != null) {
            if (mediumIncrease == instaBuy)
                mediumIncrease += 1;
            if (startingBidFinished)
                selectOptionList.add(SelectOption.of("Increase bid by: $" + Util.formatNumber(mediumIncrease), "" + mediumIncrease));
            selectOptionList.add(SelectOption.of("Insta Buy: $" + Util.formatNumber(instaBuy), "" + instaBuy));
        } else if (startingBidFinished) {
            selectOptionList.add(SelectOption.of("Increase bid by: $" + Util.formatNumber(mediumIncrease), "" + mediumIncrease));
        }

        return discordAuction.getSelectMenu(selectOptionList);
    }
}
