package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.commandManager.SubCommandGroup;
import com.alttd.util.OptionMappingParsing;
import com.alttd.util.Util;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SubCommandClose extends SubCommand {
    protected SubCommandClose(SubCommandGroup parentGroup, DiscordCommand parent) {
        super(parentGroup, parent);
    }

    @Override
    public String getName() {
        return "close";
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
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {

    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
