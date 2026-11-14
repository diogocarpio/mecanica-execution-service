package com.f1rsters.execution.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.UUID;

@Document("outbox_events")
@CompoundIndex(name = "pending_events_idx", def = "{'published': 1, 'occurredAt': 1}")
public class OutboxEvent {
    @Id
    private UUID id;
    private String eventType;
    private UUID aggregateId;
    private String payload;
    private OffsetDateTime occurredAt;
    private boolean published;

    protected OutboxEvent() {}

    public OutboxEvent(UUID aggregateId, String eventType, String payload, OffsetDateTime occurredAt) {
        this.id = UUID.randomUUID();
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.occurredAt = occurredAt;
        this.published = false;
    }

    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public UUID getAggregateId() { return aggregateId; }
    public String getPayload() { return payload; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public boolean isPublished() { return published; }

    public void markPublished() { this.published = true; }
}
