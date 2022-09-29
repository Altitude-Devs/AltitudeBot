package com.alttd.buttonManager.buttons.remindMeConfirm;

import com.alttd.database.queries.QueriesReminders.Reminder;
import net.dv8tion.jda.api.interactions.InteractionHook;

record HookAndReminder(Reminder reminder, InteractionHook interactionHook) {

}
