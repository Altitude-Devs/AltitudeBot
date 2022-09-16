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
            Logger.severe("Unable to create polls table, shutting down...");
        }
    }

    private void createOutputChannelsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS output_channels(" +
                "guild BIGINT NOT NULL, " +
                "output_type VARCHAR(64) NOT NULL, " +
                "channel BIGINT NOT NULL, " +
                "channel_type VARCHAR(64) NOT NULL, " +
                "PRIMARY KEY (guild, output_type, channel)" +
                ")";
        try {
            connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            Logger.sql(e);
            Logger.severe("Unable to create polls table, shutting down...");
        }
    }

    public static void createTables(Connection connection) {
        if (instance == null)
            instance = new DatabaseTables(connection);
    }

}
