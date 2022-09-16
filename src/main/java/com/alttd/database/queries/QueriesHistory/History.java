package com.alttd.database.queries.QueriesHistory;

public record History(HistoryType historyType, String bannedBy, String reason, long time, long until, boolean active) {
}
