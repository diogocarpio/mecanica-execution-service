package com.f1rsters.execution.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OSEvent(
        UUID eventId,
        Long osId,
        String clienteId,
        String veiculoId,
        List<String> servicosIds,
        List<String> pecasIds,
        String status,
        String eventType,
        OffsetDateTime timestamp) {
}
