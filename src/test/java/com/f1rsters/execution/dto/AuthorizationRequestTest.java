package com.f1rsters.execution.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationRequestTest {

    @Test
    void compactConstructorExposesFields() {
        Long osId = 42L;
        UUID eventId = UUID.randomUUID();

        AuthorizationRequest request = new AuthorizationRequest(osId, eventId);

        assertEquals(osId, request.osId());
        assertEquals(eventId, request.eventId());
    }

    @Test
    void convenienceConstructorGeneratesEventId() {
        AuthorizationRequest request = new AuthorizationRequest(7L);

        assertEquals(7L, request.osId());
        assertNotNull(request.eventId());
    }
}
