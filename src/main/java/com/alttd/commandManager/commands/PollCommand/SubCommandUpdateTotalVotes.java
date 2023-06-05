package com.alttd.commandManager.commands.PollCommand;

import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.commandManager.SubCommandGroup;
import com.alttd.util.Logger;
import com.alttd.util.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SubCommandUpdateTotalVotes extends SubCommand {
    private final JDA jda;
    protected SubCommandUpdateTotalVotes(SubCommandGroup parentGroup, DiscordCommand parent, JDA jda) {
        super(parentGroup, parent);
        this.jda = jda;
    }

    @Override
    public String getName() {
        return "update_total_votes";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        PollChannel pollChannel = PollUtil.getPollHandleErrors(event, getName());
        if (pollChannel == null)
            return;
        PollUtil.updatePoll(jda, Logger.altitudeLogs, pollChannel.poll());
        event.replyEmbeds(Util.genericSuccessEmbed("Success", "Updated poll [" + pollChannel.poll().getTitle() + "]")).setEphemeral(true).queue();
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
