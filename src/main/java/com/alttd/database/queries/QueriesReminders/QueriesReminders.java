package com.alttd.database.queries.QueriesReminders;

import com.alttd.database.Database;
import com.alttd.util.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class QueriesReminders {

    public static int storeReminder(Reminder reminder) {
        String sql = "INSERT INTO new_reminders " +
                "(title, description, user_id, guild_id, channel_id, message_id, should_repeat, creation_date, remind_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, reminder.title());
            preparedStatement.setString(2, reminder.description());
            preparedStatement.setLong(3, reminder.userId());
            preparedStatement.setLong(4, reminder.guildId());
            preparedStatement.setLong(5, reminder.channelId());
            preparedStatement.setLong(6, 0);
            preparedStatement.setInt(7, reminder.shouldRepeat() ? 1 : 0);
            preparedStatement.setLong(8, reminder.creationDate());
            preparedStatement.setLong(9, reminder.remindDate());

            if (preparedStatement.executeUpdate() == 1) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

            return -1;
        } catch (SQLException e) {
            Logger.exception(e);
        }
        return -1;
    }

    public static boolean removeReminder(int id) {
        String sql = "DELETE FROM new_reminders WHERE id = ?";
        try {
            PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);

            preparedStatement.setInt(1, id);

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            Logger.exception(e);
        }
        return false;
    }

    public static ArrayList<Reminder> getReminders() {
        String sql = "SELECT * FROM new_reminders";
        try {
            ArrayList<Reminder> reminders = new ArrayList<>();
            PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                reminders.add(getReminder(resultSet));
            }
            return reminders;
        } catch (SQLException e) {
            Logger.exception(e);
        }
        return null;
    }

    private static Reminder getReminder(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String title = resultSet.getString("title");
        String desc = resultSet.getString("description");
        long userId = resultSet.getLong("user_id");
        long guildId = resultSet.getLong("guild_id");
        long channelId = resultSet.getLong("channel_id");
        long messageId = resultSet.getLong("message_id");
        boolean shouldRepeat = resultSet.getInt("should_repeat") == 1;
        long creationDate = resultSet.getLong("creation_date");
        long remindDate = resultSet.getLong("remind_date");
        return new Reminder(id, title, desc, userId, guildId, channelId, messageId, shouldRepeat, creationDate, remindDate);
    }

}