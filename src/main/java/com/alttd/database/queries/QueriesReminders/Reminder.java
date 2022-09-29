package com.alttd.database.queries.QueriesReminders;

import com.alttd.util.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public record Reminder (int id, String title, String description, long userId, long guildId, long channelId,
                        long messageId, boolean shouldRepeat, long creationDate, long remindDate) {
    public TextChannel getChannel(JDA jda) {
        Guild guildById = getGuild(jda);
        if (guildById == null)
            return null;

        TextChannel textChannelById = guildById.getTextChannelById(this.channelId);
        if (textChannelById == null) {
            Logger.warning("Unable to find text channel for reminder, text channel id: [" + channelId + "]");
            return null;
        }

        return textChannelById;
    }

    public Guild getGuild(JDA jda) {
        Guild guildById = jda.getGuildById(guildId);
        if (guildById == null) {
            Logger.warning("Unable to find guild for reminder, guild id: [" + guildId + "]");
            return null;
        }

        return guildById;
    }
}
