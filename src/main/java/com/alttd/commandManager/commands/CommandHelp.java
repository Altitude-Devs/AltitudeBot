package com.alttd.commandManager.commands;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.config.MessagesConfig;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class CommandHelp extends DiscordCommand {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String execute(String[] args, Member commandSource) {
        return null;
    }

    @Override
    public String execute(String[] args, User commandSource) {
        return null;
    }

    @Override
    public String getHelpMessage() {
        return MessagesConfig.HELP_HELP;
    }

    @Override
    public List<String> getAlias() {
        return null;
    }
}
