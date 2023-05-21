package com.alttd.listeners;

import com.alttd.database.queries.QueriesLockedChannels;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class LockedChannel extends ListenerAdapter {

    private final HashMap<Long, HashSet<Long>> lockedChannels;

    public LockedChannel() {
        HashMap<Long, HashSet<Long>> tmp = QueriesLockedChannels.getLockedChannels();
        lockedChannels = Objects.requireNonNullElseGet(tmp, HashMap::new);
        if (tmp == null)
            Logger.altitudeLogs.error("Unable to load data from Locked Channels table");
    }

    public synchronized boolean lockChannel(long guildId, long channelId) {
        HashSet<Long> channels = lockedChannels.getOrDefault(guildId, new HashSet<>());
        if (channels.contains(channelId))
            return false;
        if (!QueriesLockedChannels.addLockedChannel(guildId, channelId))
            return false;
        channels.add(channelId);
        lockedChannels.put(guildId, channels);
        return true;
    }

    public synchronized boolean unlockChannel(long guildId, long channelId) {
        if (!lockedChannels.containsKey(guildId))
            return false;
        HashSet<Long> channels = lockedChannels.get(guildId);
        if (!channels.contains(channelId))
            return false;
        if (!QueriesLockedChannels.removeLockedChannel(guildId, channelId))
            return false;
        channels.remove(channelId);
        lockedChannels.put(guildId, channels);
        return true;
    }

    public synchronized boolean containsChannel(long guildId, long channelId) {
        if (!lockedChannels.containsKey(guildId))
            return false;
        HashSet<Long> channels = lockedChannels.get(guildId);
        return channels.contains(channelId);
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getAuthor().isBot() || event.getAuthor().isSystem())
            return;
        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        if (containsChannel(guildId, channelId) && event.getMember() != null && !event.getMember().isOwner())
            event.getMessage().delete().queue();
    }

}
