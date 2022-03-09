package com.alttd.commandManager;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public abstract class DiscordCommand {

    public abstract String getName();

    public String getPermission() {
        return "command." + getName();
    }

    public abstract String execute(String[] args, Member commandSource);

    public abstract String execute(String[] args, User commandSource);

    public abstract String getHelpMessage();

    public abstract List<String> getAlias();

}
