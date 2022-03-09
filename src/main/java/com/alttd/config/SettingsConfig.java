package com.alttd.config;

public class SettingsConfig extends AbstractConfig {

    static SettingsConfig settingsConfig;

    public SettingsConfig() {
        super("settings.yml");
    }

    public static void reload() {
        settingsConfig = new SettingsConfig();

        settingsConfig.readConfig(SettingsConfig.class, null);
    }

    public static String TOKEN = "token";
    private void loadSettings() {
        TOKEN = settingsConfig.getString("settings.token", TOKEN);
    }

}
