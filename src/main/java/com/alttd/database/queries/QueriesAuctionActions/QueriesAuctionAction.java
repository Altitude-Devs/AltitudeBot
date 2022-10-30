package com.alttd.database.queries.QueriesAuctionActions;

import com.alttd.database.Database;
import com.alttd.database.queries.QueriesAuctions.Auction;
import com.alttd.util.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class QueriesAuctionAction {
    public static boolean saveAuctionAction(AuctionAction auctionAction) {
        String sql = "INSERT INTO auction_actions " +
                "(message_id, action_type, user_id, price, action_time) " +
                "VALUES (?, ?, ?, ?, ?) ";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setLong(1, auctionAction.messageId());
            statement.setString(2, auctionAction.auctionType().toString());
            statement.setLong(3, auctionAction.userId());
            statement.setInt(4, auctionAction.price());
            statement.setLong(5, auctionAction.time());

            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static boolean loadAuctionAction(Auction auction) {
        String sql = "SELECT * FROM auction_actions " +
                "WHERE message_id = ?";
        long messageId = auction.getMessageId();

        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setLong(1, messageId);
            ResultSet resultSet = statement.executeQuery();

            LinkedList<AuctionAction> actions = new LinkedList<>();
            while (resultSet.next()) {
                long userId = resultSet.getLong("user_id");
                long actionTime = resultSet.getLong("action_time");
                int price = resultSet.getInt("price");
                String actionTypeString = resultSet.getString("action_type");
                AuctionType auctionType;

                try {
                    auctionType = AuctionType.valueOf(actionTypeString);
                } catch (IllegalArgumentException e) {
                    Logger.warning("Invalid auction type found in database for message: % at time: %", messageId + "", actionTime + "");
                    continue;
                }
                actions.add(new AuctionAction(auctionType, userId, messageId, price, actionTime));
            }
            auction.resetActions(actions);
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }
}
