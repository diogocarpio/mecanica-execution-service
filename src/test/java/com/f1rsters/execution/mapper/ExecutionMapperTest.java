package com.f1rsters.execution.mapper;

import com.f1rsters.execution.domain.Execution;
import com.f1rsters.execution.domain.ExecutionHistory;
import com.f1rsters.execution.domain.ExecutionStatus;
import com.f1rsters.execution.dto.ExecutionResponse;
import com.f1rsters.execution.dto.HistoryResponse;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;

class ExecutionMapperTest {

    private final ExecutionMapper mapper = new ExecutionMapper();

    @Test
    void toResponseMapsExecutionCorrectly() {
        UUID id = UUID.randomUUID();
        Long osId = 123L;
        ExecutionStatus status = ExecutionStatus.IN_DIAGNOSIS;
        
        Execution execution = Execution.queue(osId, OffsetDateTime.now());
        
        ExecutionResponse response = mapper.toResponse(execution);
        
        assertNotNull(response);
        assertEquals(execution.getId(), response.executionId());
        assertEquals(execution.getOsId(), response.osId());
        assertEquals(execution.getStatus().name(), response.status());
    }

    @Test
    void toResponseMapsExecutionHistoryCorrectly() {
        UUID executionId = UUID.randomUUID();
        ExecutionStatus fromStatus = ExecutionStatus.QUEUED;
        ExecutionStatus toStatus = ExecutionStatus.IN_DIAGNOSIS;
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        ExecutionHistory history = new ExecutionHistory(executionId, fromStatus, toStatus, occurredAt);
        
        HistoryResponse response = mapper.toResponse(history);
        
        assertNotNull(response);
        assertEquals(fromStatus.name(), response.fromStatus());
        assertEquals(toStatus.name(), response.toStatus());
        assertEquals(occurredAt, response.occurredAt());
    }

    @Test
    void toResponseHandlesNullFromStatus() {
        UUID executionId = UUID.randomUUID();
        ExecutionStatus toStatus = ExecutionStatus.QUEUED;
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        ExecutionHistory history = new ExecutionHistory(executionId, null, toStatus, occurredAt);
        
        HistoryResponse response = mapper.toResponse(history);
        
        assertNotNull(response);
        assertNull(response.fromStatus());
        assertEquals(toStatus.name(), response.toStatus());
        assertEquals(occurredAt, response.occurredAt());
    }
}
