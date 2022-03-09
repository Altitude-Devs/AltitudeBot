package com.alttd.commandManager.commands;

import com.alttd.AltitudeBot;
import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.config.MessagesConfig;
import com.alttd.permissions.PermissionManager;
import com.alttd.templates.Parser;
import com.alttd.templates.Template;
import com.alttd.util.Logger;
import com.alttd.util.Util;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.Optional;

public class CommandHelp extends DiscordCommand {

    private final CommandManager commandManager;

    public CommandHelp(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String execute(String[] args, Member commandSource, TextChannel textChannel) {
        return execute(args, textChannel, commandSource.getIdLong(), textChannel.getGuild().getIdLong(), Util.getGroupIds(commandSource));
    }

    @Override
    public String execute(String[] args, User commandSource, TextChannel textChannel) {
        if (!(textChannel instanceof PrivateChannel))
            Logger.warning("Using User when executing command on Member: % Command: %", commandSource.getAsMention(), getName());
        return execute(args, textChannel, commandSource.getIdLong(), 0, null);
    }

    public String execute(String[] args, TextChannel textChannel, long userId, long guildId, List<Long> groupIds) {
        PermissionManager permissionManager = AltitudeBot.getInstance().getPermissionManager();
        StringBuilder helpMessage = new StringBuilder();
        if (args.length == 0) {
            commandManager.getCommands().stream()
                    .filter(command -> permissionManager.hasPermission(
                            textChannel,
                            userId,
                            groupIds,
                            command.getPermission()))
                    .forEach(command -> helpMessage.append(command.getHelpMessage()));
        } else {
            String arg = args[0].toLowerCase();
            Optional<DiscordCommand> first = commandManager.getCommands().stream()
                    .filter(command -> command.getName().equals(arg)
                            || command.getAliases().contains(arg)).findFirst();
            if (first.isEmpty())
                return Parser.parse(MessagesConfig.INVALID_COMMAND_ARGS,
                        Template.of("args", arg),
                        Template.of("command", getName()),
                        Template.of("prefix", commandManager.getPrefix(guildId)));
            DiscordCommand discordCommand = first.get();
            helpMessage.append(discordCommand.getExtendedHelpMessage());
        }
        return Parser.parse(MessagesConfig.HELP_MESSAGE_TEMPLATE, Template.of("commands", helpMessage.toString()));
    }

    @Override
    public String getHelpMessage() {
        return MessagesConfig.HELP_HELP;
    }

    @Override
    public List<String> getAliases() {
        return null;
    }
}
