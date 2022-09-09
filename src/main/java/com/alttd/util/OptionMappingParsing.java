package com.alttd.util;

import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class OptionMappingParsing {

    public static String getString(String optionName, SlashCommandInteractionEvent event, String commandName) {
        OptionMapping optionMappingString = event.getInteraction().getOption(optionName);
        String text = optionMappingString == null ? null : optionMappingString.getAsString();
        if (text == null)
            event.replyEmbeds(Util.invalidCommand(commandName, "Not a valid string or didn't give input for " + optionName, event.getInteraction())).setEphemeral(true).queue();
        return text;
    }

    public static  GuildMessageChannel getGuildChannel(String optionName, SlashCommandInteractionEvent event, String commandName) {
        OptionMapping optionMappingChannel = event.getInteraction().getOption(optionName);
        GuildMessageChannel messageChannel = optionMappingChannel == null ? null : optionMappingChannel.getAsChannel().asGuildMessageChannel();
        if (messageChannel == null)
            event.replyEmbeds(Util.invalidCommand(commandName, "Not a valid text channel or didn't give input for " + optionName, event.getInteraction())).setEphemeral(true).queue();
        return messageChannel;
    }

    public static  Long getLong(String optionName, SlashCommandInteractionEvent event, String commandName) {
        OptionMapping optionMappingLong = event.getInteraction().getOption(optionName);
        if (optionMappingLong == null) {
            event.replyEmbeds(Util.invalidCommand(commandName, "Not a valid number or didn't give input for " + optionName, event.getInteraction())).setEphemeral(true).queue();
            return null;
        }
        return optionMappingLong.getAsLong();
    }
}
