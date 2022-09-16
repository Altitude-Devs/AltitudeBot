package com.alttd.database.queries.QueriesFlags;

import com.alttd.database.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QueriesFlags {

    public static List<Flag> getFlags(UUID uuid) {
        String sql = "SELECT * FROM flags_view WHERE uuid = ?";
        ArrayList<Flag> flags = new ArrayList<>();
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                flags.add(formatFlag(uuid, resultSet));
            }
            return flags;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private static Flag formatFlag(UUID uuid, ResultSet resultSet) throws SQLException {
        String reason = resultSet.getString("reason");
        long startTime = resultSet.getLong("time_flagged");
        long expireTime = resultSet.getLong("expire_time");
        long length = expireTime - startTime;
        String flaggedBy = resultSet.getString("flagged_by");
        return new Flag(uuid, reason, startTime, expireTime, length, flaggedBy);
    }

}
