package com.alttd.database.queries.commandOutputChannels;

import com.alttd.database.Database;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CommandOutputChannels {

    public static boolean setOutputChannel(long guildId, OutputType outputType, long channelId, ChannelType channelType) {
        String sql = "INSERT INTO output_channels (guild, output_type, channel, channel_type) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE channel = ?";
        try {
            PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);
            preparedStatement.setLong(1, guildId);
            preparedStatement.setString(2, outputType.name());
            preparedStatement.setLong(3, channelId);
            preparedStatement.setString(4, channelType.name());
            preparedStatement.setLong(5, channelId);

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            Logger.exception(e);
            return false;
        }
    }

    /**
     * Retrieve the channelId of the channel in the specified guild for the specified output type
     * @param guild guild to get the channel for
     * @param outputType output type to check for
     * @return long channel id or 0 if it errors or can't be found
     */
    public static GuildChannel getOutputChannel(Guild guild, OutputType outputType) {
        String sql = "SELECT channel, channel_type FROM output_channels WHERE guild = ? AND output_type = ?";
        try {
            PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);
            preparedStatement.setLong(1, guild.getIdLong());
            preparedStatement.setString(2, outputType.name());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String stringChannelType = resultSet.getString("channel_type");
                ChannelType channelType;
                try {
                    channelType = ChannelType.valueOf(stringChannelType);
                } catch (IllegalArgumentException exception) {
                    return null;
                }
                long channelId = resultSet.getLong("channel");
                switch (channelType) {
                    case TEXT, NEWS -> {
                        return guild.getTextChannelById(channelId);
                    }
                    case GUILD_NEWS_THREAD, GUILD_PUBLIC_THREAD, GUILD_PRIVATE_THREAD -> {
                        return guild.getThreadChannelById(channelId);
                    }
                    case FORUM -> {
                        return guild.getForumChannelById(channelId);
                    }
                    default -> {
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            Logger.exception(e);
        }
        return null;
    }

}

