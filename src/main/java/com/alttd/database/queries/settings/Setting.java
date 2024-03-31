package com.alttd.database.queries.settings;

import com.alttd.database.Database;

import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

public class Setting {

    private final List<Class> allowedClasses = List.of(Boolean.class, Integer.class, Long.class, Float.class, Double.class, String.class);

    public <T> void insertSetting(String key, T value, String type) throws SQLException, IllegalArgumentException {
        if (!allowedClasses.contains(type)) {
            throw new IllegalArgumentException(String.format("Invalid type, the only allowed types are: %s", allowedClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(", "))));
        }
        String query = "INSERT INTO settings (name, value, type) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection("your_connection_string");
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, key);
            preparedStatement.setString(2, value.toString());
            preparedStatement.setString(3, type);
            preparedStatement.executeUpdate();
        }
    }

    public <T> T getSetting(String key, Class<T> type) throws SQLException, IllegalArgumentException {
        if (!allowedClasses.contains(type)) {
            throw new IllegalArgumentException(String.format("Invalid type, the only allowed types are: %s", allowedClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(", "))));
        }
        String query = "SELECT value, type FROM settings WHERE name = ?";

        try (Connection connection = Database.getDatabase().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, key);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String dbType = resultSet.getString("type");
                if (!dbType.equals(type.getSimpleName())) {
                    throw new IllegalArgumentException(String.format("%s is of type %s not %s", key, dbType, type.getSimpleName()));
                }
                String value = resultSet.getString("value");
                if (type.equals(Integer.class)) {
                    return type.cast(Integer.parseInt(value));
                } else if (type.equals(Boolean.class)) {
                    return type.cast(Boolean.parseBoolean(value));
                } // and so on for other types
                else {
                    return type.cast(value);
                }
            }
        }

        throw new SQLException("Key not found in settings");
    }
}