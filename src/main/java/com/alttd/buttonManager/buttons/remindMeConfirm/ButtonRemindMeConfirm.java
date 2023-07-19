package com.alttd.buttonManager.buttons.remindMeConfirm;

import com.alttd.buttonManager.DiscordButton;
import com.alttd.database.queries.QueriesReminders.QueriesReminders;
import com.alttd.database.queries.QueriesReminders.Reminder;
import com.alttd.schedulers.ReminderScheduler;
import com.alttd.util.Util;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.HashMap;
import java.util.List;

public class ButtonRemindMeConfirm extends DiscordButton {

    private static final HashMap<Long, HookAndReminder> unconfirmedReminders = new HashMap<>();

    public static synchronized void putReminder(long id, InteractionHook defer, Reminder reminder) {
        unconfirmedReminders.put(id, new HookAndReminder(reminder, defer));
    }

    public static synchronized HookAndReminder removeReminder(long id) {
        return unconfirmedReminders.remove(id);
    }

    @Override
    public String getButtonId() {
        return "remind_me_confirm";
    }

    @Override
    public void execute(ButtonInteractionEvent event) {
        HookAndReminder hookAndReminder = removeReminder(event.getUser().getIdLong());

        if (storeReminder(hookAndReminder.reminder(), event)) {
            event.replyEmbeds(Util.genericSuccessEmbed("Success", "Your reminder was successfully created!"))
                    .setEphemeral(true).queue();
        }
        hookAndReminder.interactionHook().editOriginalComponents(List.of()).queue();
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

        reminder = Reminder.reCreateReminder(id, reminder);

        ReminderScheduler instance = ReminderScheduler.getInstance(event.getJDA());
        if (instance == null) {
            QueriesReminders.removeReminder(reminder);
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
