package com.f1rsters.execution.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutboxEventTest {

    @Test
    void deveCriarOutboxEvent() {
        UUID aggregateId = UUID.randomUUID();
        String eventType = "execution.completed.v1";
        String payload = "{\"data\":\"value\"}";
        OffsetDateTime occurredAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");

        OutboxEvent event = new OutboxEvent(aggregateId, eventType, payload, occurredAt);

        assertNotNull(event.getId());
        assertEquals(aggregateId, event.getAggregateId());
        assertEquals(eventType, event.getEventType());
        assertEquals(payload, event.getPayload());
        assertEquals(occurredAt, event.getOccurredAt());
        assertFalse(event.isPublished());
    }

    @Test
    void deveRetornarId() {
        UUID aggregateId = UUID.randomUUID();
        OffsetDateTime occurredAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");

        OutboxEvent event = new OutboxEvent(aggregateId, "test.event", "{}", occurredAt);

        assertNotNull(event.getId());
    }

    @Test
    void deveRetornarOccurredAt() {
        UUID aggregateId = UUID.randomUUID();
        OffsetDateTime occurredAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");

        OutboxEvent event = new OutboxEvent(aggregateId, "test.event", "{}", occurredAt);

        assertEquals(occurredAt, event.getOccurredAt());
    }

    @Test
    void deveRetornarEventType() {
        UUID aggregateId = UUID.randomUUID();
        String eventType = "execution.completed.v1";
        OffsetDateTime occurredAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");

        OutboxEvent event = new OutboxEvent(aggregateId, eventType, "{}", occurredAt);

        assertEquals(eventType, event.getEventType());
    }

    @Test
    void deveRetornarAggregateId() {
        UUID aggregateId = UUID.randomUUID();
        OffsetDateTime occurredAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");

        OutboxEvent event = new OutboxEvent(aggregateId, "test.event", "{}", occurredAt);

        assertEquals(aggregateId, event.getAggregateId());
    }

    @Test
    void deveRetornarPayload() {
        UUID aggregateId = UUID.randomUUID();
        String payload = "{\"data\":\"value\"}";
        OffsetDateTime occurredAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");

        OutboxEvent event = new OutboxEvent(aggregateId, "test.event", payload, occurredAt);

        assertEquals(payload, event.getPayload());
    }

    @Test
    void deveRetornarPublishedFalseInicialmente() {
        UUID aggregateId = UUID.randomUUID();
        OffsetDateTime occurredAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");

        OutboxEvent event = new OutboxEvent(aggregateId, "test.event", "{}", occurredAt);

        assertFalse(event.isPublished());
    }

    @Test
    void deveMarcarComoPublicado() {
        UUID aggregateId = UUID.randomUUID();
        OffsetDateTime occurredAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");

        OutboxEvent event = new OutboxEvent(aggregateId, "test.event", "{}", occurredAt);
        event.markPublished();

        assertTrue(event.isPublished());
    }

    @Test
    void deveGerarIdUnicoParaCadaEvento() {
        UUID aggregateId = UUID.randomUUID();
        OffsetDateTime occurredAt = OffsetDateTime.parse("2026-01-01T10:00:00Z");

        OutboxEvent event1 = new OutboxEvent(aggregateId, "test.event", "{}", occurredAt);
        OutboxEvent event2 = new OutboxEvent(aggregateId, "test.event", "{}", occurredAt);

        assertNotNull(event1.getId());
        assertNotNull(event2.getId());
        assertFalse(event1.getId().equals(event2.getId()));
    }
}
