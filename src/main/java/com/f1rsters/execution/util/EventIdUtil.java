package com.f1rsters.execution.util;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class EventIdUtil {
    private EventIdUtil() {
    }

    public static UUID resolve(UUID eventId, String rawMessage) {
        return eventId != null
                ? eventId
                : UUID.nameUUIDFromBytes(rawMessage.getBytes(StandardCharsets.UTF_8));
    }
}
