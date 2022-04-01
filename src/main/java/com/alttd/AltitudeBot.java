package com.alttd;

import com.alttd.commandManager.CommandManager;
import com.alttd.config.SettingsConfig;
import com.alttd.config.MessagesConfig;
import com.alttd.permissions.PermissionManager;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.net.URISyntaxException;

public class AltitudeBot {

    private JDA jda;
    private static AltitudeBot instance;

    public static AltitudeBot getInstance() {
        return instance;
    }

    public void main(String[] args) {
        instance = this;
        Logger.info("Starting bot...");
        initConfigs();
        try {
            jda = JDABuilder.createDefault(SettingsConfig.TOKEN).build();
        } catch (LoginException e) {
            e.printStackTrace();
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
