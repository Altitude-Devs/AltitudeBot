package com.alttd.database.queries.commandOutputChannels;

import com.alttd.database.Database;
import com.alttd.util.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CommandOutputChannels {

    public static boolean setOutputChannel(long guildId, OutputType outputType, long channelId) {
        String sql = "INSERT INTO output_channel (guild, output_type, channel) VALUES (?, ?, ?)";
        try {
            PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);
            preparedStatement.setLong(1, guildId);
            preparedStatement.setString(2, outputType.name());
            preparedStatement.setLong(3, channelId);

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            Logger.exception(e);
            return false;
        }
    }

    /**
     * Retrieve the channelId of the channel in the specified guild for the specified output type
     * @param guildId id of the guild to check in
     * @param outputType output type to check for
     * @return long channel id or 0 if it errors or can't be found
     */
    public static long getOutputChannel(long guildId, OutputType outputType) {
        String sql = "SELECT channel FROM output_channel WHERE guild = ? AND output_type = ?";
        try {
            PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);
            preparedStatement.setLong(1, guildId);
            preparedStatement.setString(2, outputType.name());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
                return resultSet.getLong("channel");
            else
                return 0L;
        } catch (SQLException e) {
            Logger.exception(e);
            return 0L;
        }
    }

}

