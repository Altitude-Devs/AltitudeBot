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
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Util {
    public static List<Long> getGroupIds(Member member) {
        if (member == null)
            return new ArrayList<>();
        return member.getRoles().stream()
                .map(Role::getIdLong)
                .collect(Collectors.toList());
    }

    public static void ignoreSuccess(Object ignoredO) {
        // IDK I thought this looked nicer in the .queue call
    }

    public static void handleFailure(Throwable failure) {
        Logger.altitudeLogs.error(failure.getMessage());
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

    public static void registerCommand(CommandManager commandManager, JDA jda, CommandData commandData, String commandName) {
        for (ScopeInfo info : commandManager.getActiveLocations(commandName)) {
            switch (info.getScope()) {
                case GLOBAL -> {
                    Logger.altitudeLogs.debug("Loading command [" + commandName + "] on global.");
                    jda.upsertCommand(commandData).queue();
//                    jda.updateCommands().addCommands(commandData).queue();
                }
                case GUILD -> {
                    Guild guildById = jda.getGuildById(info.getId());
                    if (guildById == null)
                    {
                        Logger.altitudeLogs.warning("Tried to add command " + commandName + " to invalid guild " + info.getId());
                        continue;
                    }
                    registerCommand(guildById, commandData, commandName);
                }
            }
        }
    }

    public static void registerCommand(Guild guild, CommandData commandData, String commandName) {
        Logger.altitudeLogs.debug("Loading command [" + commandName + "] on guild [" + guild.getName() + "].");
//        guild.upsertCommand(commandData).queue();
        guild.upsertCommand(commandData).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
    }

    public static void deleteCommand(Guild guild, long id, String commandName) {
        Logger.altitudeLogs.debug("Deleting command [" + commandName + "] on guild [" + guild.getName() + "].");
        guild.deleteCommandById(id).queue();
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

    public static Long parseLong(String message_id) {
        long l;
        try {
            l = Long.parseLong(message_id);
        } catch (NumberFormatException ignored) {
            return null;
        }
        return l;
    }

    public static String convertTime(long timeInMillis){
        return convertTime((int) TimeUnit.MILLISECONDS.toMinutes(timeInMillis));
    }

    private static String convertTime(int timeInMinutes) {
        int days = (int) TimeUnit.MINUTES.toDays(timeInMinutes);
        int hours = (int) (TimeUnit.MINUTES.toHours(timeInMinutes) - TimeUnit.DAYS.toHours(days));
        int minutes = (int) (TimeUnit.MINUTES.toMinutes(timeInMinutes) - TimeUnit.HOURS.toMinutes(hours)
                - TimeUnit.DAYS.toMinutes(days));

        StringBuilder stringBuilder = new StringBuilder();

        if (days != 0) {
            stringBuilder.append(days).append(days == 1 ? " day, " : " days, ");
        }
        if (hours != 0) {
            stringBuilder.append(hours).append(hours == 1 ? " hour, " : " hours, ");
        }
        stringBuilder.append(minutes).append(minutes == 1 ? " minute, " : " minutes, ");

        return stringBuilder.substring(0, stringBuilder.length() - 2);
    }

    public static String capitalize(String str) {
        if (str.length() <= 1)
            return str.toUpperCase();
        return str.toUpperCase().charAt(0) + str.toLowerCase().substring(1);
    }

    public static String formatNumber(int price) {
        return formatIntegerPart(String.valueOf(price));
    }

    public static String formatNumber(double price) {
        NumberFormat numberFormat = new DecimalFormat("0.00");
        String priceString = numberFormat.format(price);
        String[] parts = priceString.split("\\.");
        String formattedIntegerPart = formatIntegerPart(parts[0]);
        return formattedIntegerPart + "." + parts[1];
    }

    private static String formatIntegerPart(String integerPart) {
        String reversedIntegerPart = new StringBuilder(integerPart).reverse().toString();
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i + 3 < reversedIntegerPart.length()) {
            sb.append(reversedIntegerPart, i, i + 3).append(",");
            i += 3;
        }
        sb.append(reversedIntegerPart.substring(i));
        return sb.reverse().toString();
    }
}
