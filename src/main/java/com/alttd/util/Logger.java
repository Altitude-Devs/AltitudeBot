package com.alttd.util;

import com.alttd.AltitudeBot;
import com.alttd.AltitudeLogs;
import com.alttd.LogLevel;

import java.io.File;
import java.io.IOException;

public class Logger {
    public static AltitudeLogs altitudeLogs;
    static {
        Logger.altitudeLogs = new AltitudeLogs().setTimeFormat("[HH:mm:ss] ");
        try {
            Logger.altitudeLogs
                    .setLogPath(new File(AltitudeBot.getInstance().getDataFolder()) + File.separator +  "logs")
                    .setLogName("debug.log", LogLevel.DEBUG)
                    .setLogName("info.log", LogLevel.INFO)
                    .setLogName("warning.log", LogLevel.WARNING)
                    .setLogName("error.log", LogLevel.ERROR)
                    .setLogDateFormat("yyyy-MM-dd");
        } catch (IOException e) {
            Logger.altitudeLogs.error(e);
        }
    }

    public static void warning(String message, String... replacements) {
        message = replace(message, replacements);
        Logger.altitudeLogs.warning(message);
    }

    private static String replace(String message, String... replacements) {
        if (replacements == null)
            return message;
        for (String replacement : replacements) {
            message = message.replaceFirst("%", replacement);
        }
        return message;
    }

    public static void setDebugActive(boolean debug) {
        Logger.altitudeLogs.setLogLevelActive(LogLevel.DEBUG, debug);
    }
}
