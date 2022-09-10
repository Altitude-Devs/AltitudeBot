package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.commandManager.SubCommandGroup;
import com.alttd.util.OptionMappingParsing;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.ArrayList;

public class SubCommandEditTitle extends SubCommand {
    protected SubCommandEditTitle(SubCommandGroup parentGroup, DiscordCommand parent) {
        super(parentGroup, parent);
    }

    @Override
    public String getName() {
        return "edit_title";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildMessageChannel channel = OptionMappingParsing.getGuildChannel("channel", event, getName());
        if (channel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Invalid channel")).setEphemeral(true).queue();
            return;
        }

        Long messageId = Util.parseLong(OptionMappingParsing.getString("message_id", event, getName()));
        if (messageId == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Invalid message id")).setEphemeral(true).queue();
            return;
        }

        String title = OptionMappingParsing.getString("title", event, getName());
        if (title == null || title.length() > 256) {
            if (title == null)
                event.replyEmbeds(Util.genericErrorEmbed("Error", "No title found")).setEphemeral(true).queue();
            else
                event.replyEmbeds(Util.genericErrorEmbed("Error", "Title too long")).setEphemeral(true).queue();
            return;
        }

        event.replyEmbeds(Util.genericWaitingEmbed("Waiting...", "Editing poll...")).setEphemeral(true).queue(hook -> {
            channel.retrieveMessageById(messageId).queue(message -> updatePoll(message, title, hook),
                    error -> hook.editOriginalEmbeds(Util.genericErrorEmbed("Error", "Unable to find message with id [" + messageId + "].")).queue());
        });
    }

    private void updatePoll(Message message, String title, InteractionHook hook) {
        EmbedBuilder firstEmbedBuilder = Util.getFirstEmbedBuilder(message);
        if (firstEmbedBuilder == null) {
            hook.editOriginalEmbeds(Util.genericErrorEmbed("Error", "Unable to get embed from poll message."))
                    .queue();
            return;
        }
        message.editMessageEmbeds(firstEmbedBuilder.setTitle(title).build()).queue(
                resultMessage -> hook.editOriginalEmbeds(Util.genericSuccessEmbed("Success", "Updated the poll title.")).queue(),
                error -> hook.editOriginalEmbeds(Util.genericErrorEmbed("Error", "Unable to edit poll message.")).queue());
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(new ArrayList<>()).queue();
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
