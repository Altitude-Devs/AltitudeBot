package com.alttd.buttonManager.buttons.autoReminder;

import com.alttd.util.Util;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.List;

public class ButtonReminderUtil {
    public static boolean shouldExecute(Message message, ButtonInteractionEvent event) {
        if (message.getEmbeds().size() != 1) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This message has too many embeds, it should only have one?"))
                    .setEphemeral(true).queue();
            return false;
        }
        Member member = event.getMember();
        if (member == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "You are not a member?"))
                    .setEphemeral(true).queue();
            return false;
        }
        List<String> collect = member.getRoles().stream().map(Role::getName).toList();
        if (!collect.contains("Appeals")) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "You must be part of the Appeals team to use these buttons"))
                    .setEphemeral(true).queue();
            return false;
        }
        return true;
    }
}
