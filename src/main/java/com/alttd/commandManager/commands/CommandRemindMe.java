package com.alttd.commandManager.commands;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.modalManager.ModalManager;
import com.alttd.modalManager.modals.ModalRemindMe;
import com.alttd.util.Util;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.Modal;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CommandRemindMe extends DiscordCommand {

    private final CommandData commandData;
    private final ModalManager modalManager;

    public CommandRemindMe(JDA jda, CommandManager commandManager, ModalManager modalManager) {
        this.modalManager = modalManager;
        commandData = Commands.slash(getName(), "Create a reminder")
                .addOption(OptionType.CHANNEL, "channel", "The channel to send the reminder in", true)
                .addOption(OptionType.STRING, "fromnow", "How long from now the reminder should send", true, true)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)
                .setGuildOnly(true);

        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "remindme";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        TextChannel channel = getValidChannel(
                event.getInteraction().getOption("channel", OptionMapping::getAsChannel), event);
        if (channel == null)
            return;

        Long fromNow = getFromNow(event.getInteraction().getOption("fromnow", OptionMapping::getAsString), event);
        if (fromNow == null)
            return;

        Modal modal = modalManager.getModalFor("remindme");
        if (modal == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve remind me modal"))
                    .setEphemeral(true).queue();
            return;
        }

        ModalRemindMe.putData(event.getIdLong(), channel, fromNow);
        event.replyModal(modal).queue();
    }

    private TextChannel getValidChannel(GuildChannelUnion channel, SlashCommandInteractionEvent event) {
        if (channel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Couldn't find channel"))
                    .setEphemeral(true).queue();
            return null;
        }

        if (!(channel instanceof TextChannel textChannel)) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Not a valid TextChannel"))
                    .setEphemeral(true).queue();
            return null;
        }

        if (!textChannel.canTalk()) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "I can't talk in this channel"))
                    .setEphemeral(true).queue();
            return null;
        }
        return textChannel;
    }

    private Long getFromNow(String fromNow, SlashCommandInteractionEvent event) {
        if (fromNow == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Couldn't find from now option"))
                    .setEphemeral(true).queue();
            return null;
        }

        if (!fromNow.matches("[1-9][0-9]*[dmy]")) {
            return fromNowTimestamp(fromNow, event);
        }

        int i;
        try {
            i = Integer.parseInt(fromNow.substring(0, fromNow.length() - 1));
        } catch (NumberFormatException e) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Invalid number"))
                    .setEphemeral(true).queue();
            return null;
        }

        switch (fromNow.substring(fromNow.length() - 1)) {
            case "d" -> {
                return TimeUnit.DAYS.toMillis(i) + new Date().getTime();
            }
            case "m" -> {
                Calendar instance = Calendar.getInstance();
                instance.setTime(new Date());
                instance.add(Calendar.MONTH, i);
                return instance.getTimeInMillis();
            }
            case "y" -> {
                Calendar instance = Calendar.getInstance();
                instance.setTime(new Date());
                instance.add(Calendar.YEAR, i);
                return instance.getTimeInMillis();
            }
            default -> {
                event.replyEmbeds(Util.genericErrorEmbed("Error", "Invalid format? This shouldn't be possible..."))
                        .setEphemeral(true).queue();
                return null;
            }
        }
    }

    private Long fromNowTimestamp(String fromNow, SlashCommandInteractionEvent event) {
        if (!fromNow.matches("t:[1-9][0-9]*")) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Invalid from now format ex: `1d`"))
                    .setEphemeral(true).queue();
            return null;
        }
        long l;
        try {
            l = Long.parseLong(fromNow.substring(2));
        } catch (NumberFormatException e) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Invalid number"))
                    .setEphemeral(true).queue();
            return null;
        }
        return l;
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        //TODO implement suggest
    }

    @Override
    public String getHelpMessage() {
        return null;
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }
}
