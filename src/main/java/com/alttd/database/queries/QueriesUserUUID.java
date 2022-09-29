package com.alttd.database.queries;

import com.alttd.database.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class QueriesUserUUID {

    public static UUID getUUIDByUsername(String username) {
        String sql = "SELECT uuid FROM luckperms_user_view WHERE username = ?";
        try {
            PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);

            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
                return UUID.fromString(resultSet.getString("uuid"));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static String getUsernameByUUID(UUID uuid) {
        String sql = "SELECT username FROM luckperms_user_view WHERE uuid = ?";
        try {
            PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);

            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
                return resultSet.getString("username");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return "user not found";
    }
}
