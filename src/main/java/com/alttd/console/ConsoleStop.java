package com.alttd.console;

import com.alttd.AltitudeBot;
import com.alttd.util.Logger;

public class ConsoleStop extends ConsoleCommand {
    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public void execute(String command, String[] args) {
        Logger.info("Stopping bot...");
        AltitudeBot.getInstance().getJDA().cancelRequests();
        System.exit(0);
    }

    @Override
    public String getHelpMessage() {
        return "stop - Stop the bot";
    }
}
