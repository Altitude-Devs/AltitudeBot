package com.alttd.commandManager.commands;

import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.database.queries.QueriesToggleableRoles;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class CommandSetToggleableRoles extends DiscordCommand {

    private final HashMap<Long, HashSet<Long>> guildToRolesMap;
    private final CommandData commandData;

    public CommandSetToggleableRoles(JDA jda, CommandManager commandManager) {
        guildToRolesMap = QueriesToggleableRoles.getToggleableRoles();

        commandData = Commands.slash(getName(), "Set which roles can be toggled")
                .addOption(OptionType.ROLE, "role", "The role you want to toggle on/off", false)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED);

        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "settoggleableroles";
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
            String toggleableRoles = getToggleableRoles(guild);
            MessageEmbed messageEmbed = new EmbedBuilder()
                    .setTitle("Active roles")
                    .setColor(Color.GREEN)
                    .setDescription(toggleableRoles)
                    .build();

            event.replyEmbeds(messageEmbed).setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return;
        }
        OptionMapping optionMapping = options.get(0);
        Role role = optionMapping.getAsRole();
        if (containsRole(role)) {
            if (!QueriesToggleableRoles.removeRoleToggleable(role)) {
                event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to remove role from the database"))
                        .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
                return;
            }
            removeRole(role);
            event.replyEmbeds(Util.genericSuccessEmbed("Success", "Removed " + role.getAsMention() + " from the toggleable roles"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
        } else {
            if (role.hasPermission(Permission.ADMINISTRATOR) ||
                    role.hasPermission(Permission.MANAGE_ROLES) ||
                    role.hasPermission(Permission.MANAGE_CHANNEL) ||
                    role.hasPermission(Permission.MANAGE_THREADS) ||
                    role.hasPermission(Permission.MANAGE_WEBHOOKS) ||
                    role.hasPermission(Permission.MANAGE_SERVER) ||
                    role.hasPermission(Permission.MANAGE_PERMISSIONS) ||
                    role.hasPermission(Permission.MESSAGE_MANAGE) ||
                    role.hasPermission(Permission.MODERATE_MEMBERS)) {
                event.replyEmbeds(Util.genericErrorEmbed("Error", "For safety reason this bot can not add roles which have a manage or moderator permission"))
                        .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
                return;
            }
            if (!QueriesToggleableRoles.addRoleToggleable(role)) {
                event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to store role in the database"))
                        .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
                return;
            }
            addRole(role);
            event.replyEmbeds(Util.genericSuccessEmbed("Success", "Added " + role.getAsMention() + " to the toggleable roles"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
        }
    }

    private void addRole(Role role) {
        long guild = role.getGuild().getIdLong();
        HashSet<Long> set = guildToRolesMap.getOrDefault(guild, new HashSet<>());
        set.add(role.getIdLong());
        guildToRolesMap.put(guild, set);
    }

    private void removeRole(Role role) {
        long guild = role.getGuild().getIdLong();
        HashSet<Long> set = guildToRolesMap.getOrDefault(guild, new HashSet<>());
        if (set.isEmpty())
            return;
        set.remove(role.getIdLong());
        guildToRolesMap.put(guild, set);
    }

    public boolean containsRole(Role role) {
        return guildToRolesMap.getOrDefault(role.getGuild().getIdLong(), new HashSet<>()).contains(role.getIdLong());
    }

    public String getToggleableRoles(Guild guild) {
        HashSet<Long> roleIds = guildToRolesMap.get(guild.getIdLong());
        return guild.getRoles().stream()
                .filter(role -> roleIds.contains(role.getIdLong()))
                .map(Role::getAsMention)
                .collect(Collectors.joining("\n"));
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
