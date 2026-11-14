package com.f1rsters.execution.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.f1rsters.execution.domain.Execution;
import com.f1rsters.execution.domain.ExecutionStatus;
import com.f1rsters.execution.dto.AuthorizationRequest;
import com.f1rsters.execution.dto.ExecutionResponse;
import com.f1rsters.execution.dto.HistoryResponse;
import com.f1rsters.execution.mapper.ExecutionMapper;
import com.f1rsters.execution.service.ExecutionApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExecutionControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutionApplicationService service = mock(ExecutionApplicationService.class);
    private final ExecutionMapper mapper = mock(ExecutionMapper.class);
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ExecutionController(service, mapper)).build();

    @Test
    void authorizeReturnsCreatedResponse() throws Exception {
        Long osId = 123L;
        UUID eventId = UUID.randomUUID();
        AuthorizationRequest request = new AuthorizationRequest(osId, eventId);
        
        Execution execution = Execution.queue(osId, OffsetDateTime.now());
        ExecutionResponse response = new ExecutionResponse(execution.getId(), osId, ExecutionStatus.QUEUED.name());
        
        when(service.authorize(eq(osId), eq(eventId))).thenReturn(execution);
        when(mapper.toResponse(execution)).thenReturn(response);
        
        mockMvc.perform(post("/api/executions/authorization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.executionId").value(execution.getId().toString()))
                .andExpect(jsonPath("$.osId").value(osId))
                .andExpect(jsonPath("$.status").value(ExecutionStatus.QUEUED.name()));
    }

    @Test
    void authorizeWithDefaultEventId() throws Exception {
        Long osId = 123L;
        AuthorizationRequest request = new AuthorizationRequest(osId);
        
        Execution execution = Execution.queue(osId, OffsetDateTime.now());
        ExecutionResponse response = new ExecutionResponse(execution.getId(), osId, ExecutionStatus.QUEUED.name());
        
        when(service.authorize(eq(osId), any(UUID.class))).thenReturn(execution);
        when(mapper.toResponse(execution)).thenReturn(response);
        
        mockMvc.perform(post("/api/executions/authorization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void queueReturnsListOfExecutions() throws Exception {
        Execution execution1 = Execution.queue(1L, OffsetDateTime.now());
        Execution execution2 = Execution.queue(2L, OffsetDateTime.now());
        
        ExecutionResponse response1 = new ExecutionResponse(execution1.getId(), 1L, ExecutionStatus.QUEUED.name());
        ExecutionResponse response2 = new ExecutionResponse(execution2.getId(), 2L, ExecutionStatus.QUEUED.name());
        
        when(service.queue()).thenReturn(List.of(execution1, execution2));
        when(mapper.toResponse(execution1)).thenReturn(response1);
        when(mapper.toResponse(execution2)).thenReturn(response2);
        
        mockMvc.perform(get("/api/executions/queue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].osId").value(1L))
                .andExpect(jsonPath("$[1].osId").value(2L));
    }

    @Test
    void completeReturnsUpdatedExecution() throws Exception {
        UUID executionId = UUID.randomUUID();
        Execution execution = Execution.queue(123L, OffsetDateTime.now());
        execution.complete(OffsetDateTime.now());
        
        ExecutionResponse response = new ExecutionResponse(executionId, 123L, ExecutionStatus.COMPLETED.name());
        
        when(service.complete(executionId)).thenReturn(execution);
        when(mapper.toResponse(execution)).thenReturn(response);
        
        mockMvc.perform(post("/api/executions/{executionId}/complete", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").value(executionId.toString()))
                .andExpect(jsonPath("$.status").value(ExecutionStatus.COMPLETED.name()));
    }

    @Test
    void startDiagnosisReturnsUpdatedExecution() throws Exception {
        UUID executionId = UUID.randomUUID();
        Execution execution = Execution.queue(123L, OffsetDateTime.now());
        execution.startDiagnosis(OffsetDateTime.now());
        
        ExecutionResponse response = new ExecutionResponse(executionId, 123L, ExecutionStatus.IN_DIAGNOSIS.name());
        
        when(service.startDiagnosis(executionId)).thenReturn(execution);
        when(mapper.toResponse(execution)).thenReturn(response);
        
        mockMvc.perform(post("/api/executions/{executionId}/diagnosis/start", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ExecutionStatus.IN_DIAGNOSIS.name()));
    }

    @Test
    void startRepairReturnsUpdatedExecution() throws Exception {
        UUID executionId = UUID.randomUUID();
        Execution execution = Execution.queue(123L, OffsetDateTime.now());
        execution.startDiagnosis(OffsetDateTime.now());
        execution.startRepair(OffsetDateTime.now());
        
        ExecutionResponse response = new ExecutionResponse(executionId, 123L, ExecutionStatus.IN_REPAIR.name());
        
        when(service.startRepair(executionId)).thenReturn(execution);
        when(mapper.toResponse(execution)).thenReturn(response);
        
        mockMvc.perform(post("/api/executions/{executionId}/repairs/start", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ExecutionStatus.IN_REPAIR.name()));
    }

    @Test
    void cancelReturnsUpdatedExecution() throws Exception {
        UUID executionId = UUID.randomUUID();
        Execution execution = Execution.queue(123L, OffsetDateTime.now());
        execution.cancel(OffsetDateTime.now());
        
        ExecutionResponse response = new ExecutionResponse(executionId, 123L, ExecutionStatus.CANCELLED.name());
        
        when(service.cancel(executionId)).thenReturn(execution);
        when(mapper.toResponse(execution)).thenReturn(response);
        
        mockMvc.perform(post("/api/executions/{executionId}/cancel", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ExecutionStatus.CANCELLED.name()));
    }

    @Test
    void historyReturnsListOfHistoryResponses() throws Exception {
        UUID executionId = UUID.randomUUID();
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        com.f1rsters.execution.domain.ExecutionHistory history1 = 
                new com.f1rsters.execution.domain.ExecutionHistory(
                        executionId, null, ExecutionStatus.QUEUED, occurredAt);
        com.f1rsters.execution.domain.ExecutionHistory history2 = 
                new com.f1rsters.execution.domain.ExecutionHistory(
                        executionId, ExecutionStatus.QUEUED, ExecutionStatus.IN_DIAGNOSIS, occurredAt);
        
        HistoryResponse response1 = new HistoryResponse(null, ExecutionStatus.QUEUED.name(), occurredAt);
        HistoryResponse response2 = new HistoryResponse(ExecutionStatus.QUEUED.name(), ExecutionStatus.IN_DIAGNOSIS.name(), occurredAt);
        
        when(service.history(executionId)).thenReturn(List.of(history1, history2));
        when(mapper.toResponse(history1)).thenReturn(response1);
        when(mapper.toResponse(history2)).thenReturn(response2);
        
        mockMvc.perform(get("/api/executions/{executionId}/history", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].toStatus").value(ExecutionStatus.QUEUED.name()))
                .andExpect(jsonPath("$[1].toStatus").value(ExecutionStatus.IN_DIAGNOSIS.name()));
    }
}
