package com.alttd.console;

public abstract class ConsoleCommand {

    public abstract String getName();

    public abstract void execute(String command, String[] args);

    public abstract String getHelpMessage();

}
