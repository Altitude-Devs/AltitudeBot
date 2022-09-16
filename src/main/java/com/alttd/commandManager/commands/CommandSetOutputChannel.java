package com.alttd.commandManager.commands;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.database.queries.commandOutputChannels.CommandOutputChannels;
import com.alttd.database.queries.commandOutputChannels.OutputType;
import com.alttd.util.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandSetOutputChannel extends DiscordCommand {

    private final CommandData commandData;

    public CommandSetOutputChannel(JDA jda, CommandManager commandManager) {
        commandData = Commands.slash(getName(), "Set up output channels")
                .addOption(OptionType.STRING, "type", "The type of output channel", true, true)
                .addOption(OptionType.CHANNEL, "channel", "The channel the specified output should go into", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "setoutputchannel";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This command can only be used within guilds")).setEphemeral(true).queue();
            return;
        }

        OptionMapping option = event.getOption("type");
        if (option == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to find type parameter.")).setEphemeral(true).queue();
            return;
        }

        String type = option.getAsString().toUpperCase();
        OutputType outputType;
        try {
             outputType = OutputType.valueOf(type);
        } catch (IllegalArgumentException exception) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Not a valid output type.")).setEphemeral(true).queue();
            return;
        }

        option = event.getOption("channel");
        if (option == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to find channel parameter.")).setEphemeral(true).queue();
            return;
        }

        ChannelType channelType = option.getChannelType();
        switch (channelType) {
            case TEXT, NEWS, GUILD_NEWS_THREAD, GUILD_PUBLIC_THREAD, GUILD_PRIVATE_THREAD, FORUM -> {
                GuildChannelUnion channel = option.getAsChannel();
                boolean success = CommandOutputChannels.setOutputChannel(guild.getIdLong(), outputType, channel.getIdLong(), channelType);
                if (success)
                    event.replyEmbeds(Util.genericSuccessEmbed("Success", "Set channel " + channel.getAsMention() + " as the output channel for " + outputType.name() + "."))
                            .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
                else
                   event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to store the new channel output in the database"))
                           .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            }
            default -> event.replyEmbeds(Util.genericErrorEmbed("Error", "The channel type " + channelType.name() + " is not a valid output channel type"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
        }

    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        AutoCompleteQuery focusedOption = event.getFocusedOption();
        if (!focusedOption.getType().equals(OptionType.STRING) || !focusedOption.getName().equalsIgnoreCase("type"))
            event.replyChoices(Collections.emptyList()).queue();
        String value = focusedOption.getValue().toLowerCase();
        List<String> collect = Arrays.stream(OutputType.values())
                .map(Enum::name)
                .filter(type -> type.toLowerCase().startsWith(value.toLowerCase()))
                .collect(Collectors.toList());

        for (int i = collect.size(); i > 25; i--) //Can only have 25 options
            collect.remove(i - 1);
        event.replyChoiceStrings(collect).queue();
    }

    @Override
    public String getHelpMessage() {
        return null;
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }
}
