package com.alttd.commandManager.commands;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.config.MessagesConfig;
import com.alttd.templates.Parser;
import com.alttd.templates.Template;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandHelp extends DiscordCommand {

    private final CommandManager commandManager;
    private final CommandData commandData;

    public CommandHelp(JDA jda, CommandManager commandManager) {
        this.commandManager = commandManager;

        commandData = Commands.slash(getName(), "Show info about all commands or a specific command.")
                .addOption(OptionType.STRING, "command", "Command to get more info about", true , true);

        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {

        StringBuilder helpMessage = new StringBuilder();
        List<OptionMapping> options = event.getOptions();
        if (options.size() == 0) {
            commandManager.getCommands(event.getGuild()).forEach(command -> helpMessage.append(command.getHelpMessage()));
        } else {
            OptionMapping optionMapping = event.getOption("command");
            if (optionMapping == null) {
                event.replyEmbeds(new EmbedBuilder()
                                .setTitle("Error")
                                .setDescription("Missing command option")
                                .setColor(Color.RED)
                                .build())
                        .setEphemeral(true)
                        .queue();
                return;
            }
            String command = optionMapping.getAsString();
            Optional<DiscordCommand> first = commandManager.getCommands().stream()
                    .filter(discordCommand -> discordCommand.getName().equals(command)).findFirst();
            if (first.isEmpty())
            {
                event.replyEmbeds(new EmbedBuilder()
                                .setTitle("Command Not Found")
                                .setDescription("Unable to find the `" + command + "` command.")
                                .setColor(Color.RED)
                                .build())
                        .setEphemeral(true)
                        .queue();
                return;
            }
            helpMessage.append(first.get().getExtendedHelpMessage());
        }
        event.replyEmbeds(new EmbedBuilder()
                        .setTitle("Help")
                        .setDescription(Parser.parse(MessagesConfig.HELP_MESSAGE_TEMPLATE, Template.of("commands", helpMessage.toString())))
                        .setColor(Color.GREEN)
                        .build())
                .setEphemeral(true)
                .queue();
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        AutoCompleteQuery focusedOption = event.getFocusedOption();
        if (!focusedOption.getType().equals(OptionType.STRING) || !focusedOption.getName().equals("command"))
            return;
        String value = focusedOption.getValue().toLowerCase();
        List<String> collect = commandManager.getCommands().stream()
                .map(DiscordCommand::getName)
                .filter(name -> name.toLowerCase().startsWith(value))
                .collect(Collectors.toList());

        for (int i = collect.size(); i > 25; i--) //Can only have 25 options
            collect.remove(i - 1);
        event.replyChoiceStrings(collect).queue();
    }

    @Override
    public String getHelpMessage() {
        return MessagesConfig.HELP_HELP;
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }
}
