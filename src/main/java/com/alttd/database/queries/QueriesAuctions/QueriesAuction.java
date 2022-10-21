package com.alttd.database.queries.QueriesAuctions;

import com.alttd.database.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class QueriesAuction {

    public static HashMap<Long, Auction> getAuctions() {
        HashMap<Long, Auction> auctions = new HashMap<>();
        String sql = "SELECT * FROM auctions";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                long messageId = resultSet.getLong("message_id");
                long channelId = resultSet.getLong("channel_id");
                long guildId = resultSet.getLong("guild_id");
                int startingPrice = resultSet.getInt("starting_price");
                long expireTime = resultSet.getLong("expire_time");
                auctions.put(messageId, new Auction(messageId, channelId, guildId, startingPrice, expireTime));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
        return auctions;
    }

    public static boolean saveAuction(Auction auction) {
        String sql = "INSERT INTO auctions " +
                "(message_id, channel_id, guild_id, starting_price, expire_time) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE expire_time = ?";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setLong(1, auction.getMessageId());
            statement.setLong(2, auction.getChannelId());
            statement.setLong(3, auction.getGuildId());
            statement.setInt(4, auction.getStartingPrice());
            statement.setLong(5, auction.getExpireTime());
            statement.setLong(6, auction.getExpireTime());

            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static boolean removeAuction(Auction auction) {
        String sql = "DELETE FROM auctions " +
                "WHERE message_id = ?";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setLong(1, auction.getMessageId());
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }
}
