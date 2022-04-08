package com.alttd.config;

public class SettingsConfig extends AbstractConfig {

    static SettingsConfig settingsConfig;

    public SettingsConfig() {
        super("settings.yml");
    }

    public static void reload() {
        settingsConfig = new SettingsConfig();

        settingsConfig.readConfig(SettingsConfig.class, settingsConfig);
    }

    public static String TOKEN = "token";

    private void loadSettings() {
        TOKEN = settingsConfig.getString("settings.token", TOKEN);
    }

    public static String DATABASE_DRIVER = "mysql";
    public static String DATABASE_IP = "localhost";
    public static String DATABASE_PORT = "3306";
    public static String DATABASE_NAME = "discordLink";
    public static String DATABASE_USERNAME = "root";
    public static String DATABASE_PASSWORD = "root";

    private void loadDatabase() {
        DATABASE_DRIVER = settingsConfig.getString("settings.database_driver", DATABASE_DRIVER);
        DATABASE_IP = settingsConfig.getString("settings.database_ip", DATABASE_IP);
        DATABASE_PORT = settingsConfig.getString("settings.database_port", DATABASE_PORT);
        DATABASE_NAME = settingsConfig.getString("settings.database_name", DATABASE_NAME);
        DATABASE_USERNAME = settingsConfig.getString("settings.database_username", DATABASE_USERNAME);
        DATABASE_PASSWORD = settingsConfig.getString("settings.database_password", DATABASE_PASSWORD);
    }

}
