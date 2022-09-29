package com.alttd.commandManager.commands;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class CommandToggleRole extends DiscordCommand {

    private final CommandSetToggleableRoles commandSetToggleableRoles;
    private final CommandData commandData;

    public CommandToggleRole(CommandSetToggleableRoles commandSetToggleableRoles, JDA jda, CommandManager commandManager) {
        this.commandSetToggleableRoles = commandSetToggleableRoles;
        commandData = Commands.slash(getName(), "Toggle a role")
                .addOption(OptionType.ROLE, "role", "The role you want to toggle on/off (run the command without this option to see all available roles)", false)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);

        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "togglerole";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This command has to be ran in a guild"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        List<OptionMapping> options = event.getInteraction().getOptions();
        if (options.size() == 0) {
            String toggleableRoles = commandSetToggleableRoles.getToggleableRoles(guild);
            MessageEmbed messageEmbed = new EmbedBuilder()
                    .setTitle("Toggleable roles")
                    .setColor(Color.GREEN)
                    .setDescription(toggleableRoles)
                    .build();

            event.replyEmbeds(messageEmbed).setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        OptionMapping optionMapping = options.get(0);
        Role role = optionMapping.getAsRole();
        if (!commandSetToggleableRoles.containsRole(role)) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This role is not toggleable!"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        Member member = event.getMember();
        if (member == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This command has to be ran in a guild"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }

        if (member.getRoles().contains(role)) {
            guild.removeRoleFromMember(member, role).queue(success ->
                    event.replyEmbeds(Util.genericSuccessEmbed("Role removed", "You no longer have " + role.getAsMention() + "."))
                            .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure),
                    error -> event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to manage your roles."))
                            .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure));
        } else {
            guild.addRoleToMember(member, role).queue(success ->
                            event.replyEmbeds(Util.genericSuccessEmbed("Role add", "You now have " + role.getAsMention() + "."))
                                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure),
                    error -> event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to manage your roles."))
                            .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure));
        }
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(Collections.emptyList()).queue();
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
