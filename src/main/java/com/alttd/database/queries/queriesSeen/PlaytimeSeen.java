package com.alttd.database.queries.queriesSeen;

import java.util.UUID;

public class PlaytimeSeen {
    private final UUID uuid;
    private String server;
    private Long lastSeen;

    public PlaytimeSeen(UUID uuid, String server, Long lastSeen) {
        this.uuid = uuid;
        this.server = server;
        this.lastSeen = lastSeen;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getServer() {
        return server;
    }

    public Long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String server, long lastSeen) {
        this.server = server;
        this.lastSeen = lastSeen;
    }

}
