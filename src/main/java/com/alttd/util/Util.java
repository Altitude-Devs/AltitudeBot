package com.alttd.util;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.ScopeInfo;
import com.alttd.commandManager.SubOption;
import com.alttd.config.MessagesConfig;
import com.alttd.templates.Parser;
import com.alttd.templates.Template;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Util {
    public static List<Long> getGroupIds(Member member) {
        if (member == null)
            return new ArrayList<>();
        return member.getRoles().stream()
                .map(Role::getIdLong)
                .collect(Collectors.toList());
    }

    public static void handleFailure(Throwable failure) {
        Logger.warning(failure.getMessage());
    }

    public static MessageEmbed guildOnlyCommand(String commandName) {
        return new EmbedBuilder()
                .setTitle("Guild Only")
                .setDescription(Parser.parse(MessagesConfig.GUILD_ONLY_MESSAGE, Template.of("command", commandName)))
                .setColor(Color.RED)
                .build();
    }

    public static MessageEmbed noPermission(String commandName) {
        return new EmbedBuilder()
                .setTitle("No Permission")
                .setDescription(Parser.parse(MessagesConfig.NO_PERMISSION_MESSAGE, Template.of("command", commandName)))
                .setColor(Color.RED)
                .build();
    }

    public static MessageEmbed invalidCommand(String commandName, String error, SlashCommandInteraction interaction) {
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle("Invalid Command")
                .setDescription(Parser.parse(MessagesConfig.INVALID_COMMAND_ARGUMENTS,
                        Template.of("command", commandName),
                        Template.of("error", error)))
                .setColor(Color.RED);
        for (OptionMapping option : interaction.getOptions()) {
            embedBuilder.addField(option.getName(), option.getAsString(), false);
        }
        return embedBuilder.build();
    }

    public static MessageEmbed invalidSubcommand(String subcommandName) {
        return new EmbedBuilder()
                .setTitle(MessagesConfig.INVALID_SUBCOMMAND)
                .setDescription(Parser.parse(MessagesConfig.INVALID_SUBCOMMAND_DESC,
                        Template.of("subcommand", subcommandName)))
                .setColor(Color.RED)
                .build();
    }

    public static MessageEmbed genericErrorEmbed(String title, String desc) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(desc)
                .setColor(Color.RED)
                .build();
    }

    public static MessageEmbed genericSuccessEmbed(String title, String desc) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(desc)
                .setColor(Color.GREEN)
                .build();
    }

    public static MessageEmbed genericWaitingEmbed(String title, String desc) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(desc)
                .setColor(Color.BLUE)
                .build();
    }

    public static void registerCommand(CommandManager commandManager, JDA jda, SlashCommandData slashCommandData, String commandName) {
        for (ScopeInfo info : commandManager.getActiveLocations(commandName)) {
            switch (info.getScope()) {
                case GLOBAL -> jda.updateCommands().addCommands(slashCommandData).queue();
                case GUILD -> {
                    Guild guildById = jda.getGuildById(info.getId());
                    if (guildById == null)
                    {
                        Logger.warning("Tried to add command % to invalid guild %.", commandName, String.valueOf(info.getId()));
                        continue;
                    }
                    guildById.updateCommands().addCommands(slashCommandData).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
                }
                case USER -> Logger.warning("Tried to add command % to user, this is not implemented yet since I don't know how this should work.");
            }
        }
    }

    public static void registerSubOptions(HashMap<String, SubOption> subCommandMap, SubOption... subOptions) {
        for (SubOption subOption : subOptions)
            subCommandMap.put(subOption.getName(), subOption);
    }

    public static boolean validateGuildMessageChannel(SlashCommandInteraction interaction, GuildMessageChannel channel, ChannelType channelType, @NotNull Member member) {
        if (channel == null) {
            interaction.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to find the TextChannel."))
                    .setEphemeral(true)
                    .queue();
            return false;
        }
        if (channelType != null && !channel.getType().equals(channelType)) {
            interaction.replyEmbeds(Util.genericErrorEmbed("Error", "Please specify a " + channelType + " channel."))
                    .setEphemeral(true)
                    .queue();
            return false;
        }
        if (!channel.canTalk()) {
            interaction.replyEmbeds(Util.genericErrorEmbed("Error", "I can't talk in this channel."))
                    .setEphemeral(true)
                    .queue();
        }
        if (!channel.canTalk(member)) {
            interaction.replyEmbeds(Util.genericErrorEmbed("Error", "You can't talk in this channel."))
                    .setEphemeral(true)
                    .queue();
            return false;
        }
        return true;
    }

    public static EmbedBuilder getFirstEmbedBuilder(Message message) {
        if (message.getEmbeds().isEmpty())
            return null;
        MessageEmbed messageEmbed = message.getEmbeds().get(0);
        return new EmbedBuilder(messageEmbed);
    }
}
