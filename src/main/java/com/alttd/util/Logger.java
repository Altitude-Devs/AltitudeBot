package com.alttd.util;

public class Logger { //TODO make this log to a file

    private static final java.util.logging.Logger logger;

    static {
        logger = java.util.logging.Logger.getLogger("DiscordBot");
    }

    public static void info(String message, String... replacements) {
        message = replace(message, replacements);
        logger.info(message);
    }

    public static void warning(String message, String... replacements) {
        message = replace(message, replacements);
        logger.warning(message);
    }

    public static void severe(String message, String... replacements) {
        message = replace(message, replacements);
        logger.severe(message);
    }

    private static String replace(String message, String... replacements) {
        if (replacements == null)
            return message;
        for (String replacement : replacements) {
            message = message.replaceFirst("%", replacement);
        }
        return message;
    }

}
