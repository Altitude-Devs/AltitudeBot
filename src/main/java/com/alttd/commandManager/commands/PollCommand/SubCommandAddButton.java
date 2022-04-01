package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.util.OptionMappingParsing;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SubCommandAddButton extends SubCommand {
    protected SubCommandAddButton(DiscordCommand parent) {
        super(parent);
    }

    @Override
    public String getName() {
        return "add_button";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildMessageChannel channel = OptionMappingParsing.getGuildChannel("channel", event, getName());
        if (channel == null)
            return;
        Long messageId = OptionMappingParsing.getLong("message_id", event, getName());
        if (messageId == null)
            return;
        Long rowLong = OptionMappingParsing.getLong("button_row", event, getName());
        if (rowLong == null)
            return;
        int row = rowLong.intValue();
        String buttonName = OptionMappingParsing.getString("button_name", event, getName());
        if (buttonName == null)
            return;
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
