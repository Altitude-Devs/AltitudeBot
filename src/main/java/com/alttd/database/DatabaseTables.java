package com.alttd.database;

import com.alttd.util.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseTables {

    private static DatabaseTables instance = null;
    private Connection connection;

    protected DatabaseTables (Connection connection) {
        this.connection = connection;
        init(DatabaseTables.class, this);
    }

    private void init(Class<?> clazz, Object instance) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers())) {
                if (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE && method.getName().contains("Table")) {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (InvocationTargetException ex) {
                        throw new RuntimeException(ex.getCause());
                    } catch (Exception ex) {
                        Logger.severe("Error invoking %.", method.toString());
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private void createPollsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS polls(" +
                "poll_id BIGINT NOT NULL, " +
                "channel_id BIGINT NOT NULL, " +
                "guild_id BIGINT NOT NULL, " +
                "active BIT DEFAULT b'0', " +
                "poll_title VARCHAR(256) NOT NULL, " +
                "embed_type VARCHAR(32) DEFAULT 'ABSTRACT_EMBED', " +
                "PRIMARY KEY (poll_id)" +
                ")";
        try {
            connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            Logger.sql(e);
            Logger.severe("Unable to create polls table, shutting down...");
        }
    }

    private void createCommandsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS commands(" +
                "command_name VARCHAR(64) NOT NULL, " +
                "scope VARCHAR(16) NOT NULL, " +
                "location_id BIGINT NOT NULL, " +
                "PRIMARY KEY (command_name, scope, location_id)" +
                ")";
        try {
            connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            Logger.sql(e);
            Logger.severe("Unable to create commands table, shutting down...");
        }
    }

    private void createOutputChannelsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS output_channels(" +
                "guild BIGINT NOT NULL, " +
                "output_type VARCHAR(64) NOT NULL, " +
                "channel BIGINT NOT NULL, " +
                "channel_type VARCHAR(64) NOT NULL, " +
                "PRIMARY KEY (guild, output_type)" +
                ")";
        try {
            connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            Logger.sql(e);
            Logger.severe("Unable to create output channel table, shutting down...");
        }
    }

    private void createToggleableRolesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS toggleable_roles(" +
                "guild BIGINT NOT NULL, " +
                "role BIGINT NOT NULL, " +
                "PRIMARY KEY (guild, role)" +
                ")";
        try {
            connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            Logger.sql(e);
            Logger.severe("Unable to create toggleable roles table, shutting down...");
        }
    }

    private void createReminderTable() {
        String sql = "CREATE TABLE IF NOT EXISTS new_reminders(" +
                "id INT NOT NULL AUTO_INCREMENT, " +
                "title VARCHAR(256) NOT NULL, " +
                "description VARCHAR(4096) NOT NULL, " +
                "user_id LONG NOT NULL, " +
                "guild_id LONG NOT NULL, " +
                "channel_id LONG NOT NULL, " +
                "message_id LONG NOT NULL, " +
                "should_repeat TINYINT(1) NOT NULL, " +
                "creation_date LONG NOT NULL, " +
                "remind_date LONG NOT NULL, " +
                "PRIMARY KEY (id)" +
                ")";
        try {
            connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            Logger.sql(e);
            Logger.severe("Unable to create reminders table, shutting down...");
        }
    }

    private void createLockedChannelsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS locked_channels(" +
                "guild_id BIGINT NOT NULL, " +
                "channel_id BIGINT NOT NULL, " +
                "PRIMARY KEY (channel_id)" +
                ")";
        try {
            connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            Logger.sql(e);
            Logger.severe("Unable to create locked channels table, shutting down...");
        }
    }

    private void createAuctionTable() {
        String sql = "CREATE TABLE IF NOT EXISTS auctions(" +
                "message_id BIGINT NOT NULL, " +
                "channel_id BIGINT NOT NULL, " +
                "guild_id BIGINT NOT NULL, " +
                "starting_price INT NOT NULL, " +
                "expire_time BIGINT NOT NULL, " +
                "PRIMARY KEY (message_id)" +
                ")";
        try {
            connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            Logger.sql(e);
            Logger.severe("Unable to create auction table, shutting down...");
        }
    }

    public static void createTables(Connection connection) {
        if (instance == null)
            instance = new DatabaseTables(connection);
    }

}
