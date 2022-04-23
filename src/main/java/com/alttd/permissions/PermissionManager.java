package com.alttd.permissions;

import com.alttd.util.Logger;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PermissionManager {

    private static PermissionManager instance = null;
    HashMap<Long, List<String>> userPermissions;
    HashMap<Long, List<String>> groupPermissions;
    HashMap<Long, List<String>> channelEnabledCommands;
    List<String> privateEnabledCommands;

    public PermissionManager(HashMap<Long, List<String>> userPermissions,
                             HashMap<Long, List<String>> groupPermissions,
                             HashMap<Long, List<String>> channelEnabledCommands,
                             List<String> privateEnabledCommands) {
        this.userPermissions = userPermissions;
        this.groupPermissions = groupPermissions;
        this.channelEnabledCommands = channelEnabledCommands;
        this.privateEnabledCommands = privateEnabledCommands;
        instance = this;
    }

    /**
     * Check if a user has a certain permission and if that permission is enabled in the specified channel
     *
     * @param   textChannel Text channel command was executed in
     * @param   member      Member to check permission for
     * @param   permission  Permission to check for
     *
     * @return  True if the member has permission or is owner, false if not
     */
    public boolean hasPermission(TextChannel textChannel, Member member, String permission) {
        return hasPermission(textChannel,
                member.getIdLong(),
                member.getRoles().stream().map(ISnowflake::getIdLong).collect(Collectors.toList()),
                permission);
    }

    /**
     * Check if a user has a certain permission and if that permission is enabled in the specified channel
     *
     * @param   textChannel Text channel command was executed in
     * @param   userId      ID of the user to check for
     * @param   groupIds    List of group id's a user has (can be null or empty)
     * @param   permission  Permission to check for
     *
     * @return  True if the user has permission or is owner, false if not
     */
    public boolean hasPermission(TextChannel textChannel, long userId, List<Long> groupIds, String permission) {
        permission = permission.toLowerCase();
        if (textChannel instanceof PrivateChannel) {
            if (isDisabled(privateEnabledCommands, permission))
                return false;
        } else {
            if (textChannel.getGuild().getOwnerIdLong() == userId)
                return true;
            if (isDisabled(channelEnabledCommands.get(textChannel.getIdLong()), permission.toLowerCase()))
                return false;
        }
        return hasPermission(userId, groupIds, permission);
    }

    private boolean isDisabled(List<String> enabledCommandList, String permission) {
        if (enabledCommandList == null || enabledCommandList.isEmpty())
            return false;
        return !enabledCommandList.contains(permission);
    }

    private boolean hasPermission(long userId, List<Long> groupIds, String permission) {
        if (hasPermission(userPermissions.get(userId), permission))
            return true;
        if (groupIds == null || groupIds.isEmpty())
            return false;
        for (long groupId : groupIds) {
            if (hasPermission(groupPermissions.get(groupId), permission))
                return true;
        }
        return false;
    }

    private boolean hasPermission(List<String> permissions, String permission) {
        if (permission == null || permission.isEmpty())
            return false;
        return permissions.contains(permission);
    }

    /**
     * Get the permission manager instance
     *
     * @return  Permission manager instance
     */
    public static PermissionManager getInstance() {
        return instance;
    }

}
