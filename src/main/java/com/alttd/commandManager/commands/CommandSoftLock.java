package com.alttd.commandManager.commands;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.listeners.ChatListener;
import com.alttd.util.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandSoftLock extends DiscordCommand {

    private final CommandData commandData;
    private final ChatListener chatListener;

    public CommandSoftLock(JDA jda, CommandManager commandManager, ChatListener chatListener) {
        this.chatListener = chatListener;
        this.commandData = Commands.slash(getName(), "Auto delete all messages in a channel")
                .addOption(OptionType.STRING, "state", "Set the soft lock \"on\" or \"off\"", true, true)
                .addOption(OptionType.CHANNEL, "channel", "Channel to change soft lock state for", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .setGuildOnly(true);

        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "softlock";
    }

    private final List<String> states = List.of("on", "off");
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String state = event.getOption("state", OptionMapping::getAsString);
        Guild guild = event.getGuild();
        if (guild == null || state == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This command can only be used in a guild"))
                    .setEphemeral(true).queue();
            return;
        }
        GuildChannelUnion channel = event.getOption("channel", OptionMapping::getAsChannel);
        if (channel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "No valid channel was specified"))
                    .setEphemeral(true).queue();
            return;
        }
        if (!channel.getType().isMessage()) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "No valid text channel was specified"))
                    .setEphemeral(true).queue();
            return;
        }
        long guildId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        if (!event.getGuild().getSelfMember().getPermissions(channel).contains(Permission.MESSAGE_MANAGE)) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "I can't manage messages in <#" + channelId + ">"))
                    .setEphemeral(true).queue();
            return;
        }
        state = state.toLowerCase();
        switch (state) {
            case "on" -> {
                if (chatListener.containsChannel(guildId, channelId)) {
                    event.replyEmbeds(Util.genericErrorEmbed("Error", "<#" + channelId + ">" + " is already locked"))
                            .setEphemeral(true).queue();
                    break;
                }
                if (!chatListener.lockChannel(guildId, channelId))
                    event.replyEmbeds(Util.genericErrorEmbed("Error", "<#" + channelId + ">" + " could not be locked"))
                            .setEphemeral(true).queue();
                event.replyEmbeds(Util.genericSuccessEmbed("Success", "Soft locked " + "<#" + channelId + ">" + "!"))
                        .setEphemeral(true).queue();
            }
            case "off" -> {
                if (!chatListener.containsChannel(guildId, channelId)) {
                    event.replyEmbeds(Util.genericErrorEmbed("Error", "<#" + channelId + ">" + " isn't locked"))
                            .setEphemeral(true).queue();
                    break;
                }
                if (!chatListener.unlockChannel(guildId, channelId))
                    event.replyEmbeds(Util.genericErrorEmbed("Error", "<#" + channelId + ">" + " could not be unlocked"))
                            .setEphemeral(true).queue();
                event.replyEmbeds(Util.genericSuccessEmbed("Success", "Unlocked " + "<#" + channelId + ">"))
                        .setEphemeral(true).queue();
            }
            default -> event.replyEmbeds(Util.genericErrorEmbed("Error", "Invalid state"))
                    .setEphemeral(true).queue();
        }
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        String state = event.getOption("state", OptionMapping::getAsString);
        Guild guild = event.getGuild();
        if (guild == null || state == null) {
            event.replyChoiceStrings(new ArrayList<>()).queue();
            return;
        }
        String finalState = state.toLowerCase();
        event.replyChoiceStrings(states.stream()
                .filter(s -> s.startsWith(finalState))
                .collect(Collectors.toList())
        ).queue();
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
