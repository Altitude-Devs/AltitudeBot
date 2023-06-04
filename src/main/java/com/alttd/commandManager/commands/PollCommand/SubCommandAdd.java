package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.commandManager.SubCommandGroup;
import com.alttd.database.queries.Poll.PollQueries;
import com.alttd.templates.Parser;
import com.alttd.templates.Template;
import com.alttd.util.Logger;
import com.alttd.util.OptionMappingParsing;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.*;

public class SubCommandAdd extends SubCommand {

    protected SubCommandAdd(SubCommandGroup parentGroup, DiscordCommand parent) {
        super(parentGroup, parent);
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildMessageChannel channel = OptionMappingParsing.getGuildChannel("channel", event, getName());
        Member member = event.getMember();
        if (member == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to find valid guild member."))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (!Util.validateGuildMessageChannel(event.getInteraction(), channel, ChannelType.TEXT, member))
            return;
        String title = OptionMappingParsing.getString("title", event, getName());
        if (title == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve title."))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        if (title.length() > 256) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Title is too long, max 256 characters."))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.replyEmbeds(Util.genericWaitingEmbed("Creating Poll...", null))
                .setEphemeral(true)
                .queue(result -> createPoll(channel, title, result));
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {

    }

    private void createPoll(GuildMessageChannel channel, String title, InteractionHook hook) {
        channel.sendMessageEmbeds(new EmbedBuilder()
                .setTitle(title)
                .setColor(Color.RED)
                .build()
        ).queue(message -> createdPoll(message, title, hook), throwable -> failedCreatingPoll(throwable, hook));
    }

    private void createdPoll(Message message, String title, InteractionHook hook) {
        PollQueries.addPoll(message.getIdLong(), message.getChannel().getIdLong(), message.getGuild().getIdLong(), title);
        hook.editOriginalEmbeds(Util.genericSuccessEmbed("Created Poll!",
                        Parser.parse("Created a poll with the message id: `<message_id>`. " +
                                        "When you're ready don't forget to open the poll!",
                                Template.of("message_id", message.getId()))))
                .queue();
    }

    private void failedCreatingPoll(Throwable throwable, InteractionHook hook) {
        Logger.altitudeLogs.warning(throwable.getMessage());
        hook.editOriginalEmbeds(Util.genericErrorEmbed("Failed to create Poll",
                "Unable to create poll, please contact an Admin."))
                .queue();
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
