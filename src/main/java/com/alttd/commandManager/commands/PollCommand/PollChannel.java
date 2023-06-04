package com.alttd.commandManager.commands.PollCommand;

import com.alttd.database.queries.Poll.Poll;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public record PollChannel(Poll poll, TextChannel textChannel) {

}
