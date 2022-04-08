package com.alttd.config;

public class MessagesConfig extends AbstractConfig {

    static MessagesConfig messagesConfig;
    public MessagesConfig() {
        super("messages.yml");
    }
    public static void reload() {
        messagesConfig = new MessagesConfig();

        messagesConfig.readConfig(MessagesConfig.class, messagesConfig);
    }



    public static String HELP_HELP = "`/help`: Shows help menu";
    public static String HELP_MESSAGE_TEMPLATE = "<commands>";
    private static void loadHelp() {
        HELP_HELP = messagesConfig.getString("help.help", HELP_HELP);
        HELP_MESSAGE_TEMPLATE = messagesConfig.getString("help.message-template", HELP_MESSAGE_TEMPLATE);
    }
    private static void loadPollHelp() {

    }


    public static String INVALID_COMMAND = "<command> is not a valid command.";
    public static String INVALID_COMMAND_ARGS = "`<args>` is/are not valid argument(s) for `<command>`.\nFor more info see <prefix>help <command>";
    public static String INVALID_SUBCOMMAND = "Subcommand not found";
    public static String INVALID_SUBCOMMAND_DESC = "Unable to find subcommand `<subcommand>`";
    public static String GUILD_ONLY_MESSAGE = "Sorry, <command> can only be executed from within a guild.";
    public static String NO_PERMISSION_MESSAGE = "Sorry, <command> can only be executed from within a guild.";
    public static String INVALID_COMMAND_ARGUMENTS = "Some of the arguments in your command were invalid: <error>";
    private static void loadInvalidCommands() {
        INVALID_COMMAND = messagesConfig.getString("messages.invalid_command", INVALID_COMMAND);
        INVALID_COMMAND_ARGS = messagesConfig.getString("messages.invalid_command_args", INVALID_COMMAND_ARGS);
        INVALID_SUBCOMMAND = messagesConfig.getString("messages.invalid_subcommand", INVALID_SUBCOMMAND);
        GUILD_ONLY_MESSAGE = messagesConfig.getString("messages.guild_only_message", GUILD_ONLY_MESSAGE);
        NO_PERMISSION_MESSAGE = messagesConfig.getString("messages.no_permission_message", NO_PERMISSION_MESSAGE);
        INVALID_COMMAND_ARGUMENTS = messagesConfig.getString("messages.invalid_command_arguments", INVALID_COMMAND_ARGUMENTS);
    }

}
