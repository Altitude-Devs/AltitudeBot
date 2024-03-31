package com.alttd;

import com.alttd.config.MessagesConfig;
import com.alttd.config.SettingsConfig;
import com.alttd.console.ConsoleCommandManager;
import com.alttd.database.Database;
import com.alttd.database.DatabaseTables;
import com.alttd.listeners.JDAListener;
import com.alttd.util.Logger;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.net.URISyntaxException;

public class AltitudeBot {

    private JDA jda;
    @Getter
    private static AltitudeBot instance;

    public static void main(String[] args) {
        instance = new AltitudeBot();
        instance.start();
    }

    private void start() {
        Logger.altitudeLogs.info("Starting bot...");
        initConfigs();
        jda = JDABuilder.createDefault(SettingsConfig.TOKEN,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MODERATION,
                GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                GatewayIntent.GUILD_WEBHOOKS,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MESSAGE_TYPING,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.DIRECT_MESSAGE_TYPING,
                GatewayIntent.MESSAGE_CONTENT).build();
        ConsoleCommandManager.startConsoleCommands(jda);
        DatabaseTables.createTables(Database.getDatabase().getConnection());
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
        Logger.setDebugActive(SettingsConfig.DEBUG);
    }

    public String getDataFolder() {
        try {
            return new File(AltitudeBot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getPath();
        } catch (URISyntaxException e) {
            Logger.altitudeLogs.error("Unable to retrieve config directory");
            Logger.altitudeLogs.error(e);
        }
        return (null);
    }

    public JDA getJDA() {
        return jda;
    }
}
