package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.util.OptionMappingParsing;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class SubCommandEditDescription extends SubCommand {
    protected SubCommandEditDescription(DiscordCommand parent) {
        super(parent);
    }

    @Override
    public String getName() {
        return "edit_description";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildMessageChannel channel = OptionMappingParsing.getGuildChannel("channel", event, getName());
        if (channel == null)
            return;
        Long messageId = OptionMappingParsing.getLong("message_id", event, getName());
        if (messageId == null)
            return;
        String description = OptionMappingParsing.getString("description", event, getName());
        if (description == null)
            return;
    }

    //Copied over while working on add button
    private void updatePoll(GuildMessageChannel channel, int rowId, String buttonName, Message message,
                            InteractionHook hook) {
        EmbedBuilder firstEmbedBuilder = Util.getFirstEmbedBuilder(message);
        if (firstEmbedBuilder == null) {
            hook.editOriginalEmbeds(Util.genericErrorEmbed("Error", "Unable to get embed from poll message."))
                    .queue();
        }
        message.editMessageEmbeds(firstEmbedBuilder.build());//TODO finish this
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
