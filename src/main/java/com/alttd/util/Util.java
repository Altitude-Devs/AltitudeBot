package com.alttd.util;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.stream.Collectors;

public class Util {
    public static List<Long> getGroupIds(Member member) {
        return member.getRoles().stream()
                .map(Role::getIdLong)
                .collect(Collectors.toList());
    }
}
