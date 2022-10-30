package com.alttd.database.queries.QueriesAuctions;

import com.alttd.database.Database;
import com.alttd.database.queries.QueriesAuctionActions.QueriesAuctionAction;

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
                long userId = resultSet.getLong("user_id");
                int startingPrice = resultSet.getInt("starting_price");
                long expireTime = resultSet.getLong("expire_time");
                int minimumIncrease = resultSet.getInt("minimum_increase");
                Integer instaBuy = resultSet.getInt("insta_buy");
                instaBuy = instaBuy == -1 ? null : instaBuy; //Make sure the object is null if price was null or 0
                Auction auction = new Auction(userId, messageId, channelId, guildId, startingPrice, expireTime, minimumIncrease, instaBuy);
                QueriesAuctionAction.loadAuctionAction(auction);
                auctions.put(messageId, auction);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
        return auctions;
    }

    public static boolean saveAuction(Auction auction) {
        String sql = "INSERT INTO auctions " +
                "(message_id, channel_id, guild_id, user_id, starting_price, expire_time, minimum_increase, insta_buy) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE expire_time = ?";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setLong(1, auction.getMessageId());
            statement.setLong(2, auction.getChannelId());
            statement.setLong(3, auction.getGuildId());
            statement.setLong(4, auction.getUserId());
            statement.setInt(5, auction.getStartingPrice());
            statement.setLong(6, auction.getExpireTime());
            statement.setInt(7, auction.getMinimumIncrease());
            statement.setInt(8, auction.getInstaBuy() == null ? -1 : auction.getInstaBuy());
            statement.setLong(9, auction.getExpireTime());

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
