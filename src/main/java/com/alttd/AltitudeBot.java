package com.alttd;

import com.alttd.commandManager.CommandManager;
import com.alttd.config.SettingsConfig;
import com.alttd.config.MessagesConfig;
import com.alttd.permissions.PermissionManager;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.net.URISyntaxException;

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
            jda = JDABuilder.createDefault(SettingsConfig.TOKEN,
                    GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                    GatewayIntent.DIRECT_MESSAGE_TYPING,
                    GatewayIntent.DIRECT_MESSAGES,
                    GatewayIntent.GUILD_BANS,
                    GatewayIntent.GUILD_EMOJIS,
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_MESSAGE_TYPING,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_PRESENCES,
                    GatewayIntent.GUILD_WEBHOOKS).build();
        } catch (LoginException e) {
            Logger.info("Unable to log in, shutting down (check token in settings.yml).");
            exit(1);
            Logger.exception(e);
        }
        initListeners();
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
