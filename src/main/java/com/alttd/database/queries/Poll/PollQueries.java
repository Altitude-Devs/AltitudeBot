package com.alttd.database.queries.Poll;

import com.alttd.buttonManager.ButtonManager;
import com.alttd.database.Database;
import com.alttd.util.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PollQueries {

    public static boolean addPoll(long pollId, long channelId, long guildId, String title) {
        new Poll(pollId, channelId, guildId, false, title);
        String sql = "INSERT INTO polls (poll_id, channel_id, guild_id, active, poll_title, embed_type) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setLong(1, pollId);
            statement.setLong(2, channelId);
            statement.setLong(3, guildId);
            statement.setInt(4, 0);
            statement.setString(5, title);
            statement.setString(6, "POLL_EMBED");

            return statement.execute();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static boolean setPollStatus(long pollId, boolean active, ButtonManager buttonManager) {
        String sql = "UPDATE polls SET active = ? WHERE poll_id = ?";
        try {
            Poll poll = Poll.getPoll(pollId);
            if (poll == null) {
                Logger.altitudeLogs.warning("Received null poll in setPollStatus (query)");
                return false;
            }
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setInt(1, active ? 1 : 0);
            statement.setLong(2, pollId);
            poll.setActive(true);
            PollButtonQueries.loadButtons(poll, buttonManager);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static void loadPolls(ButtonManager buttonManager) {
        String sql = "SELECT * FROM polls";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Poll poll = new Poll(
                        resultSet.getLong("poll_id"),
                        resultSet.getLong("channel_id"),
                        resultSet.getLong("guild_id"),
                        resultSet.getInt("active") == 1,
                        resultSet.getString("poll_title"));
                PollButtonQueries.loadButtons(poll, buttonManager);
                Logger.altitudeLogs.debug("Loaded poll: " + poll.getTitle());
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
