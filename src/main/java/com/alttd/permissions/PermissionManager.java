package com.alttd.permissions;

import com.alttd.util.Logger;
import net.dv8tion.jda.api.entities.*;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PermissionManager {

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
    }

    public boolean hasPermission(TextChannel textChannel, long userId, List<Long> groupIds, String permission) {
        permission = permission.toLowerCase();
        if (textChannel instanceof PrivateChannel) {
            if (isDisabled(privateEnabledCommands, permission))
                return false;
        } else {
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

}
