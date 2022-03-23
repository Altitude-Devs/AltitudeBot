package com.alttd.commandManager.commands.embed;

import com.alttd.AltitudeBot;
import com.alttd.commandManager.DiscordCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.List;

public class CommandEmbed extends DiscordCommand {

    CommandEmbed() {
        CommandData commandData = new CommandData(getName(), "A command to create embeds for polls etc");
        commandData.setDefaultEnabled(false);
        commandData.addSubcommands(new SubcommandData("add", "Add a new embed to a channel")
                .addOption(OptionType.CHANNEL, "target",
                        "The channel to send the new embed in", true)
                .addOption(OptionType.STRING, "type", "Either `poll` or `default`")
                .addOption(OptionType.STRING, "text", "The description for your poll, max 1000 characters"));

        AltitudeBot.getInstance().getJDA().upsertCommand(commandData).queue();
    }

    @Override
    public String getName() {
        return "embed";
    }

    @Override
    public String execute(String[] args, Member commandSource, TextChannel textChannel) {
        return null;
    }

    @Override
    public String execute(String[] args, User commandSource, TextChannel textChannel) {
        return null;
    }

    @Override
    public String getHelpMessage() {
        return null;
    }

    @Override
    public List<String> getAliases() {
        return null;
    }
}
