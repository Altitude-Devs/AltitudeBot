package com.alttd;

import com.alttd.commandManager.CommandManager;
import com.alttd.config.SettingsConfig;
import com.alttd.config.MessagesConfig;
import com.alttd.console.ConsoleCommandManager;
import com.alttd.database.Database;
import com.alttd.database.DatabaseTables;
import com.alttd.permissions.PermissionManager;
import com.alttd.util.Logger;
import com.mysql.cj.log.Log;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Scanner;

import static java.lang.System.exit;

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
        try {
            jda = JDABuilder.createDefault(SettingsConfig.TOKEN).build();
        } catch (LoginException e) {
            Logger.info("Unable to log in, shutting down (check token in settings.yml).");
            exit(1);
            Logger.exception(e);
        }
        DatabaseTables.createTables(Database.getDatabase().getConnection());
        ConsoleCommandManager.startConsoleCommands(jda);
        try {
            jda.getPresence().setPresence(
                    OnlineStatus.valueOf(SettingsConfig.STATUS),
                    Activity.listening(SettingsConfig.ACTIVITY));
        } catch (IllegalArgumentException e) {
            Logger.exception(e);
        }
        initListeners();
        new Thread("Console Thread") { // to gracefully shutdown if using intellij
            @Override
            public void run() {
                Scanner scanner = new Scanner(System.in);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.equalsIgnoreCase("exit")) {
                        System.exit(0);
                    }
                }
            }
        }.start();
        //TODO init permissionManager
    }

    private void initListeners() {
        jda.addEventListener(new CommandManager(jda));
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
