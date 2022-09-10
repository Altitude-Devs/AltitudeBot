package com.alttd.config;

import net.dv8tion.jda.api.entities.Activity;

public class SettingsConfig extends AbstractConfig {

    static SettingsConfig settingsConfig;

    public SettingsConfig() {
        super("settings.yml");
    }

    public static void reload() {
        settingsConfig = new SettingsConfig();

        settingsConfig.readConfig(SettingsConfig.class, settingsConfig);
    }

    // SETTINGS
    public static String TOKEN = "token";
    public static boolean DEBUG = false;

    private void loadSettings() {
        TOKEN = settingsConfig.getString("settings.token", TOKEN);
        DEBUG = settingsConfig.getBoolean("settings.debug", DEBUG);
    }

    // DATABASE
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

    // ACTIVITY
    public static String STATUS = "ONLINE";
    public static String ACTIVITY = "Testing";

    private void loadActivity() {
        STATUS = settingsConfig.getString("settings.status", STATUS);
        ACTIVITY = settingsConfig.getString("settings.activity", ACTIVITY);
    }

    public static void setActivity(String newActivity) {
        ACTIVITY = newActivity;
        settingsConfig.set("settings.activity", ACTIVITY);
    }

    public static void setStatus(String status) {
        STATUS = status;
        settingsConfig.set("settings.activity", STATUS);
    }

}
