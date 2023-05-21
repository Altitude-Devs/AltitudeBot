package com.alttd.console;

import com.alttd.config.MessagesConfig;
import com.alttd.config.SettingsConfig;
import com.alttd.util.Logger;

public class ConsoleReload extends ConsoleCommand {
    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void execute(String command, String[] args) {
        if (args.length != 2) {
            Logger.altitudeLogs.info("Invalid argument length: " + getHelpMessage());
            return;
        }
        switch (args[1]) {
            case "config" -> {
                MessagesConfig.reload();
                Logger.altitudeLogs.info("Reloaded Messages config.");
                SettingsConfig.reload();
                Logger.altitudeLogs.info("Reloaded Settings config.");
            }
            case "database" -> Logger.altitudeLogs.info("NOT IMPLEMENTED YET");
            default -> Logger.altitudeLogs.info("Invalid argument: " + args[1]);
        }
    }

    @Override
    public String getHelpMessage() {
        return "reload <config/database> - Reload the configs or databases";
    }
}
