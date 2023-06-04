package com.alttd.database.queries.Poll;

import com.alttd.database.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public class PollButtonClicksQueries {

    public static boolean addButtonClick(long pollId, long buttonId, long userId) {
        String sql = "INSERT INTO poll_entries (poll_id, button_id, user_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE button_id = ?";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setLong(1, pollId);
            statement.setLong(2, buttonId);
            statement.setLong(3, userId);
            statement.setLong(4, buttonId);

            return statement.execute();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static boolean removeButtonClick(long pollId, long userId) {
        String sql = "DELETE FROM poll_entries WHERE poll_id = ? AND user_id = ?";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setLong(1, pollId);
            statement.setLong(2, userId);

            return statement.execute();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static HashMap<Long, HashSet<Long>> loadClicks(long pollId) {
        String sql = "SELECT button_id,user_id FROM poll_entries WHERE poll_id = ?";
        HashMap<Long, HashSet<Long>> buttonMap = new HashMap<>();
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setLong(1, pollId);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long buttonId = resultSet.getLong("button_id");
                HashSet<Long> userSet = buttonMap.getOrDefault(buttonId, new HashSet<>());
                userSet.add(resultSet.getLong("user_id"));
                buttonMap.put(buttonId, userSet);
            }
            return buttonMap;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
