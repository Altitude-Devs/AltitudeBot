package com.alttd.database.queries.Poll;

import com.alttd.buttonManager.ButtonManager;
import com.alttd.buttonManager.buttons.pollButton.PollButton;
import com.alttd.database.Database;
import com.alttd.util.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PollButtonQueries {

    public static boolean addButton(long pollId, String buttonId, String buttonName/*TODO: , String buttonType*/) {
        String sql = "INSERT INTO poll_buttons (poll_id, button_id, button_name, button_type) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setLong(1, pollId);
            statement.setString(2, buttonId);
            statement.setString(3, buttonName);
            statement.setString(4, "POLL_BUTTON"); //TODO make this change to the other thing too?

            return statement.execute();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static List<PollButton> loadButtons(Poll poll, ButtonManager buttonManager) {
        HashMap<Long, HashSet<Long>> userClicks = PollButtonClicksQueries.loadClicks(poll.getPollId());
        if (userClicks == null) {
            Logger.altitudeLogs.warning("Unable to load userClicks for poll with id: " + poll);
            return null;
        }
        String sql = "SELECT * FROM poll_buttons WHERE poll_id = ?";
        try {
            PreparedStatement statement = Database.getDatabase().getConnection().prepareStatement(sql);
            statement.setLong(1, poll.getPollId());
            List<PollButton> buttons = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                long internalButtonId = resultSet.getLong("id");
                PollButton pollButton = new PollButton(internalButtonId, resultSet.getString("button_id"), resultSet.getString("button_name"), userClicks.getOrDefault(internalButtonId, new HashSet<>()));
                buttonManager.addButton(pollButton);
                buttons.add(pollButton);
            }
            poll.addButtons(buttons);
            return buttons;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

}
