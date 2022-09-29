package com.alttd.database.queries.queriesSeen;

import com.alttd.database.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SeenQueries {

    public static PlaytimeSeen getLastSeen(UUID uuid) {

        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement("SELECT server_name, last_seen FROM playtime WHERE uuid = ? AND last_seen IS NOT NULL ORDER BY last_seen DESC LIMIT 1");
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                PlaytimeSeen playtimeSeen = new PlaytimeSeen(uuid, resultSet.getString("server_name"), resultSet.getLong("last_seen"));
                return playtimeSeen;
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
