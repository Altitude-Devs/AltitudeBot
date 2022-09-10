package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.commandManager.SubCommandGroup;
import com.alttd.templates.Parser;
import com.alttd.templates.Template;
import com.alttd.util.Logger;
import com.alttd.util.OptionMappingParsing;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class SubCommandAddButton extends SubCommand {
    protected SubCommandAddButton(SubCommandGroup parentGroup, DiscordCommand parent) {
        super(parentGroup, parent);
    }

    @Override
    public String getName() {
        return "add_button";
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

        Long messageId = Util.parseLong(OptionMappingParsing.getString("message_id", event, getName()));
        if (messageId == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Invalid message id")).setEphemeral(true).queue();
            return;
        }
        //TODO verify that message id is in database

        Long rowLong = Util.parseLong(OptionMappingParsing.getString("button_row", event, getName()));
        if (rowLong == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve button row."))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        int rowId = rowLong.intValue();
        if (rowId < 1 || rowId > 5) {
            event.replyEmbeds(Util.genericErrorEmbed("Error",
                            Parser.parse("Invalid row id `<id>`, only 1-5 are valid.",
                                    Template.of("id", String.valueOf(rowId)))))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        String buttonName = OptionMappingParsing.getString("button_name", event, getName());
        if (buttonName == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve button name."))
                    .setEphemeral(true)
                    .queue();
            return;
        }
        event.deferReply().queue(hook ->
                channel.retrieveMessageById(messageId).queue(
                        message -> updatePoll(channel, rowId, buttonName, message, hook),
                        throwable -> failedToGetMessage(throwable, hook)));
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {

    }

    private void failedToGetMessage(Throwable throwable, InteractionHook hook) {
        Logger.warning(throwable.getMessage());
        hook.editOriginalEmbeds(Util.genericErrorEmbed("Failed to get poll message",
                        "Please check if the poll still exists and the message id is correct."))
                .queue();
    }

    private void updatePoll(GuildMessageChannel channel, int rowId, String buttonName, Message message,
                            InteractionHook hook) {
        //TODO add button, generate id, add a way to remove button, store id in database
        //Maybe temporarily disable the poll and listen for someone to click the button, then delete that button
        // and switch back to normal poll mode?
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
