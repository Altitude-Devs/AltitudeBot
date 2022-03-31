package com.alttd.util;

import com.alttd.AltitudeBot;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;

public class Logger { //TODO make this log to a file

    private static final java.util.logging.Logger info;
    private static final java.util.logging.Logger error;
    private static final java.util.logging.Logger sql;

    static {
        File logDir = new File(AltitudeBot.getInstance().getDataFolder() + File.pathSeparator +  "logs");
        if (!logDir.exists())
        {
            try {
                if (!logDir.createNewFile() || !logDir.mkdir()) {
                    System.out.println("UNABLE TO CREATE LOGGING DIRECTORY");
                    System.exit(1);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        info = java.util.logging.Logger.getLogger("info");
        error = java.util.logging.Logger.getLogger("error");
        sql = java.util.logging.Logger.getLogger("sql");
        info.setLevel(Level.ALL);
        error.setLevel(Level.ALL);
        sql.setLevel(Level.ALL);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd");
        Date date = new Date();
        String formattedTime = dateFormat.format(date.getTime());

        try {
            info.addHandler(new FileHandler(logDir.getAbsolutePath() + File.pathSeparator +
                    formattedTime + "info.log"));
            error.addHandler(new FileHandler(logDir.getAbsolutePath() + File.pathSeparator +
                    formattedTime + "error.log"));
            sql.addHandler(new FileHandler(logDir.getAbsolutePath() + File.pathSeparator +
                    formattedTime + "sql.log"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void info(String message, String... replacements) {
        message = replace(message, replacements);
        info.info(message);
    }

    public static void warning(String message, String... replacements) {
        message = replace(message, replacements);
        error.warning(message);
    }

    public static void severe(String message, String... replacements) {
        message = replace(message, replacements);
        error.severe(message);
    }

    public static void sql(String message) {
        sql.info(message);
    }

    public static void sql(SQLException exception) {
        exception.printStackTrace();
        sql.info("SQLState: " + exception.getSQLState() + "\n");
        sql.severe("Error:\n" + exception.getMessage());
    }

    public static void exception(Exception exception) {
        exception.printStackTrace();
        error.severe("Error:\n" + exception.getMessage());
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
