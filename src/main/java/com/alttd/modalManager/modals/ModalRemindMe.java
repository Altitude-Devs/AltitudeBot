package com.alttd.modalManager.modals;

import com.alttd.buttonManager.ButtonManager;
import com.alttd.buttonManager.buttons.remindMeConfirm.ButtonRemindMeConfirm;
import com.alttd.database.queries.QueriesReminders.Reminder;
import com.alttd.database.queries.QueriesReminders.ReminderType;
import com.alttd.modalManager.DiscordModal;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.TimeUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ModalRemindMe extends DiscordModal {

    private static final HashMap<Long, RemindMeData> userToRemindMeMap = new HashMap<>();

    public static synchronized void putData(long userId, TextChannel channel, long timestamp) {
        userToRemindMeMap.put(userId, new RemindMeData(channel, timestamp));
    }

    private static synchronized RemindMeData pullData(long userId) {
        return userToRemindMeMap.remove(userId);
    }

    private final ButtonManager buttonManager;

    public ModalRemindMe(ButtonManager buttonManager) {
        this.buttonManager = buttonManager;
    }

    @Override
    public String getModalId() {
        return "remindme";
    }

    @Override
    public void execute(ModalInteractionEvent event) {
        String title = getValidString(event.getValue("title"), event, true);
        if (title == null)
            return;

        String desc = getValidString(event.getValue("description"), event, false);
        if (desc == null)
            desc = "";

        long userId = event.getUser().getIdLong();
        RemindMeData remindMeData = pullData(userId);
        if (remindMeData == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Couldn't find the data from the command that triggered this modal"))
                    .setEphemeral(true).queue();
            return;
        }

        Reminder reminder = new Reminder(
                -1,
                title,
                desc,
                userId,
                remindMeData.textChannel.getGuild().getIdLong(),
                remindMeData.textChannel.getIdLong(),
                0,
                false,
                new Date().getTime(),
                remindMeData.timestamp,
                ReminderType.MANUAL,
                null);

        Button remindMeConfirm = buttonManager.getButtonFor("remind_me_confirm");
        Button remindMeCancel = buttonManager.getButtonFor("remind_me_cancel");
        if (remindMeConfirm == null || remindMeCancel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve continue/cancel buttons"))
                    .setEphemeral(true).queue();
            return;
        }

        MessageEmbed messageEmbed = new EmbedBuilder()
                .setTitle(reminder.title())
                .setDescription(reminder.description())
                .appendDescription("\n\nWill remind <t:" + TimeUnit.MILLISECONDS.toSeconds(reminder.remindDate()) + ":R>")
                .build();
        event.deferReply().setEphemeral(true).queue(defer -> {
            ButtonRemindMeConfirm.putReminder(userId, defer, reminder);
            defer.editOriginalEmbeds(messageEmbed).queue(message ->
                    defer.editOriginalComponents().setActionRow(remindMeConfirm, remindMeCancel)
                            .queue(RestAction.getDefaultSuccess(), Util::handleFailure));
        });
    }

    public String getValidString(ModalMapping modalMapping, ModalInteractionEvent event, boolean required) {
        if (modalMapping == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Couldn't find modal"))
                    .setEphemeral(true).queue();
            return null;
        }

        String string = modalMapping.getAsString();
        if (string.isEmpty()) {
            if (required)
                event.replyEmbeds(Util.genericErrorEmbed("Error", "Couldn't find contents of modal"))
                        .setEphemeral(true).queue();
            return null;
        }

        return string;
    }

    @Override
    public Modal getModal() {
        TextInput title = TextInput.create("title", "Title", TextInputStyle.SHORT)
                .setPlaceholder("reminder title")
                .setRequiredRange(1, 256)
                .setRequired(true)
                .build();

        TextInput desc = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
                .setPlaceholder("optional reminder description")
                .setRequiredRange(1, 4000)
                .setRequired(false)
                .build();

        return Modal.create(getModalId(), "Remind Me")
                .addActionRows(ActionRow.of(title), ActionRow.of(desc))
                .build();
    }

    private record RemindMeData(TextChannel textChannel, long timestamp) {}
}
