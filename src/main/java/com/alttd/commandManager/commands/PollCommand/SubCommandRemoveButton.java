package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.commandManager.SubCommandGroup;
import com.alttd.util.Util;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SubCommandRemoveButton extends SubCommand {
    protected SubCommandRemoveButton(SubCommandGroup parentGroup, DiscordCommand parent) {
        super(parentGroup, parent);
    }

    @Override
    public String getName() {
        return "remove_button";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyEmbeds(Util.genericErrorEmbed("Error", "The code to remove buttons was never implemented")).setEphemeral(true).queue();
//        PollChannel pollChannel = PollUtil.getPollHandleErrors(event, getName());
//        if (pollChannel == null)
//            return;
//        String buttonName = OptionMappingParsing.getString("button_name", event, getName());
//        if (buttonName == null)
//            return;
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        PollUtil.handleSuggestMessageId(event);
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
