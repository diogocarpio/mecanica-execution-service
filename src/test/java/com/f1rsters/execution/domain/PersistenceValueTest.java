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
        assertEquals("IN_DIAGNOSIS", history.getToStatus());
        assertEquals(eventId, inbox.getEventId());
        assertEquals("execution.authorized.v1", inbox.getEventType());
        assertEquals(now, history.getOccurredAt());
        assertEquals(now, inbox.getProcessedAt());
        assertNotNull(history.getId());
    }

    @Test
    void protectedConstructorsAreAvailableForPersistence() {
        assertNotNull(new Execution());
        assertNotNull(new OutboxEvent());
        assertNotNull(new ExecutionHistory());
        assertNotNull(new InboxProcessedMessage());
    }

    @Test
    void executionExposesItsPersistenceFields() {
        Execution execution = new Execution();

        assertNull(execution.getId());
        assertNull(execution.getOsId());
        assertNull(execution.getStatus());
        assertNull(execution.getApprovedAt());
        assertNull(execution.getCreatedAt());
        assertNull(execution.getUpdatedAt());
    }
}
