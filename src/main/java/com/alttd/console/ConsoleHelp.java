package com.alttd.console;

import com.alttd.templates.Parser;
import com.alttd.templates.Template;
import com.alttd.util.Logger;

import java.util.stream.Collectors;

public class ConsoleHelp extends ConsoleCommand {
    ConsoleCommandManager commandManager;

    public ConsoleHelp(ConsoleCommandManager instance) {
        super();
        commandManager = instance;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void execute(String command, String[] args) {
        Template template = Template.of("commands", commandManager.getCommands().stream()
                .map(ConsoleCommand::getHelpMessage)
                .collect(Collectors.joining("\n")));
        Logger.info(Parser.parse("Commands:\n<commands>", template));
    }

    @Override
    public String getHelpMessage() {
        return "help - Shows this help menu";
    }
}
