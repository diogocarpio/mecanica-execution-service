package com.f1rsters.execution.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionStatusTest {

    @Test
    void enumContainsAllExpectedValues() {
        ExecutionStatus[] values = ExecutionStatus.values();
        assertEquals(5, values.length);
        assertTrue(java.util.Arrays.asList(values).contains(ExecutionStatus.QUEUED));
        assertTrue(java.util.Arrays.asList(values).contains(ExecutionStatus.IN_DIAGNOSIS));
        assertTrue(java.util.Arrays.asList(values).contains(ExecutionStatus.IN_REPAIR));
        assertTrue(java.util.Arrays.asList(values).contains(ExecutionStatus.COMPLETED));
        assertTrue(java.util.Arrays.asList(values).contains(ExecutionStatus.CANCELLED));
    }

    @Test
    void enumValuesAreCorrectlyNamed() {
        assertEquals("QUEUED", ExecutionStatus.QUEUED.name());
        assertEquals("IN_DIAGNOSIS", ExecutionStatus.IN_DIAGNOSIS.name());
        assertEquals("IN_REPAIR", ExecutionStatus.IN_REPAIR.name());
        assertEquals("COMPLETED", ExecutionStatus.COMPLETED.name());
        assertEquals("CANCELLED", ExecutionStatus.CANCELLED.name());
    }
}
