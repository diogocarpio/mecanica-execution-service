package com.f1rsters.execution.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EventIdUtilTest {

    @Test
    void resolveReturnsProvidedEventIdWhenNotNull() {
        UUID eventId = UUID.randomUUID();
        String rawMessage = "test message";
        
        UUID result = EventIdUtil.resolve(eventId, rawMessage);
        
        assertEquals(eventId, result);
    }

    @Test
    void resolveGeneratesUuidFromMessageWhenEventIdIsNull() {
        String rawMessage = "test message";
        
        UUID result = EventIdUtil.resolve(null, rawMessage);
        
        assertNotNull(result);
        assertEquals(UUID.nameUUIDFromBytes(rawMessage.getBytes(java.nio.charset.StandardCharsets.UTF_8)), result);
    }

    @Test
    void resolveGeneratesConsistentUuidForSameMessage() {
        String rawMessage = "consistent message";
        
        UUID result1 = EventIdUtil.resolve(null, rawMessage);
        UUID result2 = EventIdUtil.resolve(null, rawMessage);
        
        assertEquals(result1, result2);
    }

    @Test
    void resolveGeneratesDifferentUuidsForDifferentMessages() {
        String message1 = "message one";
        String message2 = "message two";
        
        UUID result1 = EventIdUtil.resolve(null, message1);
        UUID result2 = EventIdUtil.resolve(null, message2);
        
        assertNotEquals(result1, result2);
    }
}
