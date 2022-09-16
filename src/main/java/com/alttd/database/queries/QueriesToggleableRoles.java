package com.alttd.database.queries;

import com.alttd.database.Database;
import net.dv8tion.jda.api.entities.Role;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

public class QueriesToggleableRoles {

    public static boolean addRoleToggleable(Role role) {
        String sql = "INSERT INTO toggleable_roles (guild, role) VALUES (?, ?)";
        try {
            PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);

            preparedStatement.setLong(1, role.getGuild().getIdLong());
            preparedStatement.setLong(2, role.getIdLong());

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static boolean removeRoleToggleable(Role role) {
        String sql = "DELETE FROM toggleable_roles WHERE guild = ? AND role = ?";
        try {
            PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);

            preparedStatement.setLong(1, role.getGuild().getIdLong());
            preparedStatement.setLong(2, role.getIdLong());

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public static HashMap<Long, HashSet<Long>> getToggleableRoles() {
        String sql = "SELECT * FROM toggleable_roles";
        try {
            HashMap<Long, HashSet<Long>> map = new HashMap<>();
            PreparedStatement preparedStatement = Database.getDatabase().getConnection().prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                long guild = resultSet.getLong("guild");
                long role = resultSet.getLong("role");
                HashSet<Long> roles = map.getOrDefault(guild, new HashSet<>());
                roles.add(role);
                map.put(guild, roles);
            }

            return map;
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

}
