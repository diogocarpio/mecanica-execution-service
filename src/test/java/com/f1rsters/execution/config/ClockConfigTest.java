package com.f1rsters.execution.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;

class ClockConfigTest {

    @Test
    void clockBeanReturnsSystemUTCClock() {
        ClockConfig config = new ClockConfig();
        Clock clock = config.clock();
        
        assertNotNull(clock);
        assertEquals("Z", clock.getZone().getId());
    }
}
