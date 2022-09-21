package com.alttd.buttonManager.buttons.remindMeConfirm;

import com.alttd.buttonManager.DiscordButton;
import com.alttd.database.queries.QueriesReminders.QueriesReminders;
import com.alttd.database.queries.QueriesReminders.Reminder;
import com.alttd.reminders.ReminderScheduler;
import com.alttd.util.Util;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.HashMap;

public class ButtonRemindMeConfirm extends DiscordButton {

    private static final HashMap<Long, Reminder> unconfirmedReminders = new HashMap<>();

    public static synchronized void putReminder(long id, Reminder reminder) {
        unconfirmedReminders.put(id, reminder);
    }

    public static synchronized Reminder removeReminder(long id) {
        return unconfirmedReminders.get(id);
    }

    @Override
    public String getButtonId() {
        return "remind_me_confirm";
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        Reminder reminder = removeReminder(event.getUser().getIdLong());

        if (storeReminder(reminder, event)) {
            event.replyEmbeds(Util.genericSuccessEmbed("Success", "Your reminder was successfully created!"))
                    .setEphemeral(true).queue();
        }
    }

    private boolean storeReminder(Reminder reminder, ButtonInteractionEvent event) {
        if (reminder == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve reminder data for this button"))
                    .setEphemeral(true).queue();
            return false;
        }
        int id = QueriesReminders.storeReminder(reminder);
        if (id == 0) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to store reminder in the database"))
                    .setEphemeral(true).queue();
            return false;
        }

        reminder = new Reminder(
                id,
                reminder.title(),
                reminder.description(),
                reminder.userId(),
                reminder.guildId(),
                reminder.channelId(),
                reminder.messageId(),
                reminder.shouldRepeat(),
                reminder.creationDate(),
                reminder.remindDate());

        ReminderScheduler instance = ReminderScheduler.getInstance(event.getJDA());
        if (instance == null) {
            QueriesReminders.removeReminder(reminder.id());
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to start reminder, removing it from the database..."))
                    .setEphemeral(true).queue();
            return false;
        }

        instance.addReminder(reminder);
        return true;
    }

    @Override
    public Button getButton() {
        return Button.success(getButtonId(), "Confirm");
    }
}
