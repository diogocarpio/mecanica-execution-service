package com.f1rsters.execution.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SagaMessage(
        UUID eventId,
        String eventType,
        int schemaVersion,
        OffsetDateTime occurredAt,
        String correlationId,
        String causationId,
        UUID sagaId,
        Long osId,
        UUID executionId) {
}
