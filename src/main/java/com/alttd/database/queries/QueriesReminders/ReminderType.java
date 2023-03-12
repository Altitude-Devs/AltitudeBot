package com.alttd.database.queries.QueriesReminders;

public enum ReminderType {
    NONE(-1),
    MANUAL(0),
    APPEAL(1);
    private final int id;

    ReminderType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ReminderType getReminder(int id) {
        ReminderType[] values = ReminderType.values();
        if (values.length < id) {
            return NONE;
        }
        return values[id];
    }
}
