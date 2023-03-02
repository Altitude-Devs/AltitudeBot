package com.alttd.database.queries;

import com.alttd.database.Database;
import com.alttd.util.Logger;
import com.google.protobuf.StringValue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QueriesAssignAppeal {

    /**
     * Get the user to assign an appeal to, and sets the next user to have one assigned to them
     * @return a userId to assign an appeal to or -1 if it fails
     */
    public static long getAssignAppeal() {
        String sql = "SELECT userId FROM appeal_list WHERE next = ?";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setInt(1, 1);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                long userId = resultSet.getLong("userId");
                setNextAppeal(userId);
                return userId;
            } else {
                resetAssignedAppeal();
                return -1;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return -1;
    }

    /**
     * Get the first user with a userId bigger than the number we gave
     * @param userId The id that's smaller than the next one we want to find
     * @return a userId bigger than the given one or -1 if it can't find any
     */
    private static long selectNextAssignment(long userId) {
        String sql = "SELECT * FROM appeal_list WHERE userId > ? LIMIT 1";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setLong(1, userId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("userId");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return -1;
    }

    /**
     * Get the next user in the list or the first one if we reached the and and assign them the appeal
     *  if there's no one to assign to it logs a warning
     */
    private static void setNextAppeal(long userId) {
        long nextUserId = selectNextAssignment(userId);
        if (nextUserId == -1) {
            nextUserId = selectNextAssignment(0);
            if (nextUserId == -1) {
                Logger.warning("No one to assign appeals to!");
                return;
            }
        }
        String sql1 = "UPDATE appeal_list SET next = ? WHERE userId = ?";
        String sql2 = "UPDATE appeal_list SET next = ? WHERE userId = ?";
        try {
            PreparedStatement statement1 = Database.getDatabase().getConnection().prepareStatement(sql1);
            statement1.setInt(1, 0);
            statement1.setLong(2, userId);
            PreparedStatement statement2 = Database.getDatabase().getConnection().prepareStatement(sql2);
            statement2.setInt(1, 1);
            statement2.setLong(2, nextUserId);

            int res1 = statement1.executeUpdate();
            int res2 = statement2.executeUpdate();
            if (res1 != 1 || res2 != 1) {
                Logger.warning("Unable to assign next appeal but got no error? results: " + res1 + res2);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Get the first user in the list and assign them the appeal
     *  if there's no one to assign to it logs a warning
     */
    private static void resetAssignedAppeal() {
        long userId = selectNextAssignment(0);
        if (userId == -1) {
            Logger.warning("No one to assign appeals to!");
        } else {
            setNextAppeal(userId);
        }
    }

}
