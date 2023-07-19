package com.alttd.database.queries.QueriesReminders;

import com.alttd.util.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.Channel;

import java.util.Arrays;

public record Reminder (int id, String title, String description, long userId, long guildId, long channelId,
                        long messageId, boolean shouldRepeat, long creationDate, long remindDate, ReminderType reminderType, byte[] data) {

    public static Reminder reCreateReminder(int id, Reminder reminder) {
        return new Reminder(
                id,
                reminder.title(),
                reminder.description(),
                reminder.userId(),
                reminder.guildId(),
                reminder.channelId(),
                reminder.messageId(),
                reminder.shouldRepeat(),
                reminder.creationDate(),
                reminder.remindDate(),
                reminder.reminderType(),
                reminder.data());
    }

    public Channel getChannel(JDA jda) {
        Guild guildById = getGuild(jda);
        if (guildById == null)
            return null;

        Channel channelById = guildById.getTextChannelById(this.channelId);
        if (channelById == null)
            channelById = guildById.getThreadChannelById(this.channelId);
        if (channelById == null) {
            Logger.altitudeLogs.warning("Unable to find text channel for reminder, text channel id: [" + channelId + "]");
            return null;
        }

        return channelById;
    }

    public Guild getGuild(JDA jda) {
        Guild guildById = jda.getGuildById(guildId);
        if (guildById == null) {
            Logger.altitudeLogs.warning("Unable to find guild for reminder, guild id: [" + guildId + "]");
            return null;
        }

        return guildById;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "\nid=[" + id + "]" +
                "\ntitle=[" + title  + "]" +
                "\ndescription=[" + description + "]" +
                "\nuserId=[" + userId + "]" +
                "\nguildId=[" + guildId + "]" +
                "\nchannelId=[" + channelId + "]" +
                "\nmessageId=[" + messageId + "]" +
                "\nshouldRepeat=[" + shouldRepeat + "]" +
                "\ncreationDate=[" + creationDate + "]" +
                "\nremindDate=[" + remindDate + "]" +
                "\nreminderType=[" + reminderType + "]" +
                "\ndata=[" + Arrays.toString(data) + "]" +
                '}';
    }
}
