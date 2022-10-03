package com.alttd.database.queries;

import com.alttd.database.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public class QueriesLockedChannels {

    public static boolean addLockedChannel(long guildId, long channelId) {
        String sql = "INSERT INTO locked_channels (guild_id, channel_id) VALUES (?, ?)";
        try {
            PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);

            preparedStatement.setLong(1, guildId);
            preparedStatement.setLong(2, channelId);

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static boolean removeLockedChannel(long guildId, long channelId) {
        String sql = "DELETE FROM locked_channels WHERE guild_id = ? AND channel_id = ?";
        try {
            PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);

            preparedStatement.setLong(1, guildId);
            preparedStatement.setLong(2, channelId);

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static HashMap<Long, HashSet<Long>> getLockedChannels() {
        String sql = "SELECT * FROM locked_channels";
        try {
            HashMap<Long, HashSet<Long>> map = new HashMap<>();
            PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                long guildId = resultSet.getLong("guild_id");
                long channelId = resultSet.getLong("channel_id");
                HashSet<Long> channels = map.getOrDefault(guildId, new HashSet<>());
                channels.add(channelId);
                map.put(guildId, channels);
            }

            return map;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
