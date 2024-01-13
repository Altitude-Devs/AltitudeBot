package com.alttd.database.queries;

import com.alttd.database.Database;
import com.alttd.util.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class QueriesEconomy {

    public static CompletableFuture<Optional<Double>> getBalance(UUID uuid) {
        String sql = "SELECT Balance FROM grove_balance WHERE player_uuid = ?";
        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);

                preparedStatement.setString(1, uuid.toString());
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next())
                    return Optional.of(resultSet.getDouble("Balance"));
            } catch (SQLException exception) {
                Logger.altitudeLogs.error(exception);
                exception.printStackTrace();
            }
            return Optional.empty();
        });
    }

}
