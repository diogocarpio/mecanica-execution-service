package com.f1rsters.execution.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AuthorizationRequest(@NotNull @Positive Long osId, UUID eventId) {
    public AuthorizationRequest(Long osId) {
        this(osId, UUID.randomUUID());
    }
}
