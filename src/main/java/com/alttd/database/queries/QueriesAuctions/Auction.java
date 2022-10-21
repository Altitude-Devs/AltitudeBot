package com.alttd.database.queries.QueriesAuctions;

import com.alttd.AltitudeBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Auction implements Comparable {
    private final long messageId, channelId, guildId;
    private final int startingPrice;

    private long expireTime;

    public Auction(long messageId, long channelId, long guildId, int startingPrice, long expireTime) {
        this.messageId = messageId;
        this.channelId = channelId;
        this.guildId = guildId;
        this.startingPrice = startingPrice;
        this.expireTime = expireTime;
    }

    public Auction(@NotNull Message message, long channelId, long guildId, int startingPrice, long expireTime) {
        this.messageId = message.getIdLong();
        this.channelId = channelId;
        this.guildId = guildId;
        this.startingPrice = startingPrice;
        this.expireTime = expireTime;
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
}
