package com.alttd.database.queries;

import com.alttd.database.Database;
import com.alttd.util.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class QueriesUserDiscordId {

    public static CompletableFuture<Optional<UUID>> getUUIDById(long userId) {
        String sql = "SELECT player_uuid FROM linked_accounts WHERE discord_id = ?";
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);

                preparedStatement.setLong(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next())
                    return Optional.of(UUID.fromString(resultSet.getString("player_uuid")));
            } catch (SQLException exception) {
                Logger.altitudeLogs.error(exception);
            }
            return Optional.empty();
        });
    }


}
