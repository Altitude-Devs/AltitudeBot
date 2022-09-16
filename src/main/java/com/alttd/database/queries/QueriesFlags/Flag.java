package com.alttd.database.queries.QueriesFlags;

import java.util.UUID;

public record Flag(UUID uuid, String reason, long startTime, long expireTime, long length, String flaggedBy) {
}
