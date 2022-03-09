package com.alttd.commandManager;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CommandManager extends ListenerAdapter {

    private final List<DiscordCommand> commands;
    private final HashMap<Long, String> commandPrefixes;

    public CommandManager() {
        commands = List.of();
        commandPrefixes = null;//TODO query;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] s = event.getMessage().getContentRaw().split(" ");
        if (s.length < 1)
            return;
        String command = s[0];
        String[] args = Arrays.copyOfRange(s, 1, s.length);
    }

}
