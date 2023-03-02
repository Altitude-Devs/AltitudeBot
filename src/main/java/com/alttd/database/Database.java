package com.alttd.database;

import com.alttd.config.SettingsConfig;
import com.alttd.util.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class Database {
    private static Database instance = null;
    private Connection connection;

    private Database() {
        instance = this;

        try {
            instance.openConnection();
        } catch (Exception e) {
            Logger.exception(e);
        }
    }

    private void openConnection() throws SQLException {
        if (connection != null && !connection.isClosed() && connection.isValid(50)) {
            return;
        }

        synchronized (this) {
//            if (connection != null && !connection.isClosed()) {
//                return;
//            }
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Logger.exception(e);
            }
            connection = DriverManager.getConnection(
                    "jdbc:" + SettingsConfig.DATABASE_DRIVER
                            + "://" + SettingsConfig.DATABASE_IP
                            + ":" + SettingsConfig.DATABASE_PORT
                            + "/"
                            + SettingsConfig.DATABASE_NAME,
                    SettingsConfig.DATABASE_USERNAME,
                    SettingsConfig.DATABASE_PASSWORD);
        }
    }

    /**
     * Returns the connection for the database
     * @return Returns the connection.
     */
    public Connection getConnection() {
        try {
            instance.openConnection();
        }
        catch (SQLException e) {
            Logger.sql(e);
        }
        catch (Exception e){
            Logger.exception(e);
        }

        return instance.connection;
    }

    /**
     * Sets the connection for this instance
     */
    public static Database getDatabase() {
        return Objects.requireNonNullElseGet(instance, Database::new);
    }
}
