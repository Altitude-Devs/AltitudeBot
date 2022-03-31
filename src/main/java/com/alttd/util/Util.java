package com.alttd.util;

import com.alttd.config.MessagesConfig;
import com.alttd.templates.Parser;
import com.alttd.templates.Template;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class Util {
    public static List<Long> getGroupIds(Member member) {
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
}
