package com.alttd.database.queries.QueriesHistory;

import com.alttd.database.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QueriesHistory {

    public static List<History> getHistory(UUID uuid) {
        List<History> historyList = getBans(uuid);
        if (historyList == null) {
            return null;
        }

        List<History> tmp = getMutes(uuid);
        if (tmp == null)
            return null;
        historyList.addAll(tmp);

        tmp = getKicks(uuid);
        if (tmp == null)
            return null;
        historyList.addAll(tmp);

        tmp = getWarns(uuid);
        if (tmp == null)
            return null;
        historyList.addAll(tmp);
        return historyList;
    }

    public static List<History> getBans(UUID uuid) {
        String sql = "SELECT * FROM bans_view WHERE uuid = ?";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();
            ArrayList<History> historyList = new ArrayList<>();
            while (resultSet.next()) {
                historyList.add(storeHistory(HistoryType.BAN, resultSet));
            }
            return historyList;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static List<History> getMutes(UUID uuid) {
        String sql = "SELECT * FROM mutes_view WHERE uuid = ?";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();
            ArrayList<History> historyList = new ArrayList<>();
            while (resultSet.next()) {
                historyList.add(storeHistory(HistoryType.MUTE, resultSet));
            }
            return historyList;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static List<History> getKicks(UUID uuid) {
        String sql = "SELECT * FROM kicks_view WHERE uuid = ?";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();
            ArrayList<History> historyList = new ArrayList<>();
            while (resultSet.next()) {
                historyList.add(storeHistory(HistoryType.KICK, resultSet));
            }
            return historyList;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public static List<History> getWarns(UUID uuid) {
        String sql = "SELECT * FROM warnings_view WHERE uuid = ?";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();
            ArrayList<History> historyList = new ArrayList<>();
            while (resultSet.next()) {
                historyList.add(storeHistory(HistoryType.WARN, resultSet));
            }
            return historyList;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private static History storeHistory(HistoryType historyType, ResultSet resultSet) throws SQLException {
        return new History(historyType,
                resultSet.getString("banned_by_name"),
                resultSet.getString("reason"),
                resultSet.getLong("time"),
                resultSet.getLong("until"),
                resultSet.getInt("active") == 1);
    }

}
