package com.alttd.console;

import com.alttd.config.SettingsConfig;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ConsoleStatus extends ConsoleCommand{

    private final JDA jda;

    public ConsoleStatus(JDA jda) {
        this.jda = jda;
    }

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public void execute(String command, String[] args) {
        if (args.length == 1) {
            OnlineStatus status = jda.getPresence().getStatus();
            Logger.info("Current status: " + status.getKey());
            return;
        }
        if (args.length != 2) {
            Logger.info("Invalid argument length.");
            return;
        }
        try {
            OnlineStatus status = OnlineStatus.fromKey(args[1].toLowerCase());
            SettingsConfig.setStatus(status.getKey());
            jda.getPresence().setStatus(status);
            Logger.info("Set status to: " + SettingsConfig.STATUS);
        } catch (IllegalArgumentException exception) {
            Logger.info("Invalid status please use any of the following "
                    + Arrays.stream(OnlineStatus.values())
                    .map(OnlineStatus::getKey)
                    .collect(Collectors.joining(", ")));
        }
    }

    @Override
    public String getHelpMessage() {
        return "status [set] [status] - Display the current status or set it";
    }
}
