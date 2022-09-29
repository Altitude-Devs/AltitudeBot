package com.alttd.database.queries.queriesSeen;

import com.alttd.database.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SeenQueries {

    public static List<PlaytimeSeen> getLastSeen(UUID uuid) {
        String sql = "SELECT server_name, last_seen " +
                "FROM playtime_view " +
                "WHERE uuid = ? AND last_seen IS NOT NULL " +
                "ORDER BY last_seen ASC";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();
            ArrayList<PlaytimeSeen> playtimeSeenList = new ArrayList<>();
            while (resultSet.next()) {
                playtimeSeenList.add(new PlaytimeSeen(uuid, resultSet.getString("server_name"), resultSet.getLong("last_seen")));
            }
            return playtimeSeenList;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
