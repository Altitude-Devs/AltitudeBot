package com.alttd.config;

public class MessagesConfig extends AbstractConfig {

    static MessagesConfig messagesConfig;

    public MessagesConfig() {
        super("messages.yml");
    }

    public static void reload() {
        messagesConfig = new MessagesConfig();

        messagesConfig.readConfig(MessagesConfig.class, null);
    }

    public static String HELP_HELP = "`<prefix>help`: Shows help menu";
    public static String HELP_MESSAGE_TEMPLATE = "<commands>";
    private static void loadHelp() {
        HELP_HELP = messagesConfig.getString("help.help", HELP_HELP);
        HELP_MESSAGE_TEMPLATE = messagesConfig.getString("help.message-template", HELP_MESSAGE_TEMPLATE);
    }

}
