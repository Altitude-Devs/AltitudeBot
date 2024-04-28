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
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

@SpringBootApplication
public class AltitudeBot {

    private JDA jda;
    @Getter
    private static AltitudeBot instance;
    private static String path;


    public static void main(String[] args) {
        if (args.length == 0) { //TODO change scripts so it works with this
            System.out.println("Please give the location for the configs as an arg");
            return;
        }
        path = args[0];
        instance = new AltitudeBot();
        instance.start(args);
//        SpringApplication.run(AltitudeBot.class, args);
        new SpringApplicationBuilder(AltitudeBot.class)
                .web(WebApplicationType.SERVLET) // .REACTIVE, .SERVLET
                .run(args);
    }

    private void start(String[] args) {
//        Logger.altitudeLogs.info("Starting spring application");
//        SpringApplication.run(AltitudeBot.class, args);
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
        initListeners(args);
    }

    private void initListeners(String[] args) {
        jda.addEventListener(new JDAListener(jda, args));
    }

    private void initConfigs() {
        SettingsConfig.reload();
        MessagesConfig.reload();
        Logger.setDebugActive(SettingsConfig.DEBUG);
    }

    public String getDataFolder() {
        try {
//            return new File(AltitudeBot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getPath();
            File file = Path.of(new URI("file://"+ path)).toFile();
            if (!file.exists() || !file.canWrite() || !file.isDirectory()) {
                System.out.println("Directory does not exist or can't be written to: " + file.getPath());
                return null;
            }
            return file.getPath();
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
