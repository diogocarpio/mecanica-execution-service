package com.f1rsters.execution.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.UUID;

@Document("inbox_processed_messages")
public class InboxProcessedMessage {
    @Id
    private UUID eventId;
    private String eventType;
    private OffsetDateTime processedAt;

    protected InboxProcessedMessage() {}

    public InboxProcessedMessage(UUID eventId, String eventType, OffsetDateTime processedAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.processedAt = processedAt;
    }

    public UUID getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public OffsetDateTime getProcessedAt() { return processedAt; }
}
