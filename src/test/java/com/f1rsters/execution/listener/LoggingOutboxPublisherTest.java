package com.f1rsters.execution.listener;

import com.f1rsters.execution.domain.OutboxEvent;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LoggingOutboxPublisherTest {

    @Test
    void publishDoesNotThrowException() {
        LoggingOutboxPublisher publisher = new LoggingOutboxPublisher();
        
        UUID id = UUID.randomUUID();
        String eventType = "execution.completed";
        String payload = "{\"test\":\"data\"}";
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        OutboxEvent event = new OutboxEvent(id, eventType, payload, occurredAt);
        
        assertDoesNotThrow(() -> publisher.publish(event));
    }
}
