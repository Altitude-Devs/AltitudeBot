package com.alttd.console;

import com.alttd.util.Logger;
import net.dv8tion.jda.api.JDA;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleCommandManager {

    private final List<ConsoleCommand> commands = new ArrayList<>();
    private static ConsoleCommandManager instance = null;

    private ConsoleCommandManager(JDA jda) {
        commands.addAll(List.of(
                new ConsoleActivity(jda),
                new ConsoleHelp(this),
                new ConsoleReload(),
                new ConsoleStatus(jda),
                new ConsoleStop()));
        new Thread(() -> {
            while (true)
                instance.readCommand(new Scanner(System.in));
        }).start();
    }

    private void readCommand(Scanner scanner) {
        System.out.print("command: ");
        String[] args = scanner.nextLine().toLowerCase().split(" +");
        if (args.length == 0 || args[0].length() == 0)
            return;
        String command = args[0];
        Optional<ConsoleCommand> first = commands.stream()
                .filter(consoleCommand -> consoleCommand.getName().equalsIgnoreCase(command))
                .findFirst();
        if (first.isEmpty()) {
            Logger.info("Invalid command, see help for more info.");
            return;
        }
        first.get().execute(command, args);
    }

    protected List<ConsoleCommand> getCommands() {
        return commands;
    }

    public static void startConsoleCommands(JDA jda) {
        Logger.info("Starting console commands");
        if (instance == null)
            instance = new ConsoleCommandManager(jda);
    }
}
