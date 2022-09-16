package com.alttd;

import com.alttd.config.MessagesConfig;
import com.alttd.config.SettingsConfig;
import com.alttd.console.ConsoleCommandManager;
import com.alttd.database.Database;
import com.alttd.database.DatabaseTables;
import com.alttd.listeners.JDAListener;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.io.File;
import java.net.URISyntaxException;

public class AltitudeBot {

    private JDA jda;
    private static AltitudeBot instance;

    public static AltitudeBot getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        instance = new AltitudeBot();
        instance.start();
    }

    private void start() {
        Logger.info("Starting bot...");
        initConfigs();
        ConsoleCommandManager.startConsoleCommands(jda);
        jda = JDABuilder.createDefault(SettingsConfig.TOKEN).build();
        DatabaseTables.createTables(Database.getDatabase().getConnection());
        ConsoleCommandManager.startConsoleCommands(jda);
//        try {
//            jda.getPresence().setPresence(
//                    OnlineStatus.valueOf(SettingsConfig.STATUS),
//                    Activity.listening(SettingsConfig.ACTIVITY));
//        } catch (IllegalArgumentException e) {
//            Logger.exception(e);
//        }
        initListeners();
    }

    private void initListeners() {
        jda.addEventListener(new JDAListener(jda));
    }

    private void initConfigs() {
        SettingsConfig.reload();
        MessagesConfig.reload();
    }

    public String getDataFolder() {
        try {
            return new File(AltitudeBot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getPath();
        } catch (URISyntaxException e) {
            Logger.severe("Unable to retrieve config directory");
            e.printStackTrace();
        }
        return (null);
    }

    public JDA getJDA() {
        return jda;
    }
}
