package com.f1rsters.execution.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceValueTest {
    @Test
    void outboxStoresAndPublishesItsPayload() {
        UUID aggregate = UUID.randomUUID();
        OutboxEvent event = new OutboxEvent(aggregate, "execution.completed.v1", "{}", OffsetDateTime.now());

        assertEquals(aggregate, event.getAggregateId());
        assertEquals("execution.completed.v1", event.getEventType());
        assertFalse(event.isPublished());
        event.markPublished();
        assertTrue(event.isPublished());
    }

    @Test
    void historyAndInboxExposeTheirIdentity() {
        UUID executionId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        ExecutionHistory history = new ExecutionHistory(executionId, ExecutionStatus.QUEUED,
                ExecutionStatus.IN_DIAGNOSIS, now);
        InboxProcessedMessage inbox = new InboxProcessedMessage(eventId, "execution.authorized.v1", now);

        assertEquals(executionId, history.getExecutionId());
        assertEquals("QUEUED", history.getFromStatus());
        assertEquals(eventId, inbox.getEventId());
        assertEquals("execution.authorized.v1", inbox.getEventType());
    }
}
