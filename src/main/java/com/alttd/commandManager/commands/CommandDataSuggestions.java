package com.alttd.commandManager.commands;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.util.ExcelWriter;
import com.alttd.util.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CommandDataSuggestions extends DiscordCommand {

    private final CommandData commandData;
    private final CommandManager commandManager;

    public CommandDataSuggestions(JDA jda, CommandManager commandManager) {
        this.commandManager = commandManager;
        this.commandData = Commands.slash(getName(), "Get data about suggestions from the forum channel")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .setGuildOnly(true);
        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "data_suggestions";
    }

    private void filterAndStoreActiveSuggestions(InteractionHook reply, HashSet<ThreadChannel> activeSuggestions) {
        ExcelWriter excelWriter = new ExcelWriter();
        long waitSeconds = 0;
        for (ThreadChannel activeSuggestion : activeSuggestions) {
            activeSuggestion.retrieveParentMessage().queueAfter(waitSeconds, TimeUnit.SECONDS, message -> {
                MessageReaction thumbsUp = message.getReaction(Emoji.fromUnicode("\uD83D\uDC4D"));
                MessageReaction thumbsDown = message.getReaction(Emoji.fromUnicode("\uD83D\uDC4E"));
                excelWriter.addRow(
                        message.getJumpUrl(),
                        activeSuggestion.getName(),
                        thumbsUp == null ? "err" : String.valueOf(thumbsUp.getCount()),
                        thumbsDown == null ? "err" : String.valueOf(thumbsDown.getCount()));
            });
            waitSeconds += 5;
        }
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> excelWriter.saveAndSend(reply), waitSeconds, TimeUnit.SECONDS);
    }

    private void processSuggestionThreads(SlashCommandInteractionEvent event, ForumChannel forumChannel, ForumTag unansweredTag, List<ThreadChannel> activeSuggestions) {
        activeSuggestions.addAll(forumChannel.getThreadChannels());
        activeSuggestions = activeSuggestions.stream()
                .filter(threadChannel -> !threadChannel.isLocked())
                .filter(threadChannel -> threadChannel.getAppliedTags().contains(unansweredTag))
                .toList();
        HashSet<ThreadChannel> activeSuggestionsSet = new HashSet<>(activeSuggestions);

        event.deferReply(true).queue(reply -> filterAndStoreActiveSuggestions(reply, activeSuggestionsSet));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This command needs to be executed from a guild")).queue();
            return;
        }
        ForumChannel forumChannel = guild.getForumChannelById(1019660718867488768L);
        if (forumChannel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Can't find the forum channel in this guild")).queue();
            return;
        }
        List<ForumTag> tagList = forumChannel.getAvailableTagsByName("unanswered", true);
        if (tagList.size() != 1) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Expected one unanswered tag found: " + tagList.size())).queue();
            return;
        }

        ForumTag unansweredTag = tagList.get(0);
        forumChannel.retrieveArchivedPublicThreadChannels().queue(threads -> {
            processSuggestionThreads(event, forumChannel, unansweredTag, threads);
        });
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(List.of()).queue();
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
