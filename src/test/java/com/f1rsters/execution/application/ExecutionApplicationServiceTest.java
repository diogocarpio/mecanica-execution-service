package com.f1rsters.execution.servide;

import com.f1rsters.execution.repository.ExecutionHistoryRepository;
import com.f1rsters.execution.repository.ExecutionRepository;
import com.f1rsters.execution.repository.OutboxEventRepository;
import com.f1rsters.execution.repository.InboxProcessedMessageRepository;
import com.f1rsters.execution.domain.Execution;
import com.f1rsters.execution.domain.ExecutionStatus;
import com.f1rsters.execution.exception.ExecutionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;

class ExecutionApplicationServiceTest {
    private ExecutionRepository executions;
    private ExecutionHistoryRepository history;
    private OutboxEventRepository outbox;
    private InboxProcessedMessageRepository inbox;
    private ExecutionApplicationService service;

    @BeforeEach
    void setUp() {
        executions = mock(ExecutionRepository.class);
        history = mock(ExecutionHistoryRepository.class);
        outbox = mock(OutboxEventRepository.class);
        inbox = mock(InboxProcessedMessageRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
        service = new ExecutionApplicationService(executions, history, outbox, inbox, clock);
    }

    @Test
    void authorizationIsIdempotentByOs() {
        Execution execution = Execution.queue(10L, java.time.OffsetDateTime.now());
        when(executions.findByOsId(10L)).thenReturn(Optional.of(execution));

        assertEquals(execution, service.authorize(10L));
        verify(executions, never()).save(any());
    }

    @Test
    void completionCreatesHistoryAndOutbox() {
        Execution execution = Execution.queue(10L, java.time.OffsetDateTime.now());
        UUID id = execution.getId();
        when(executions.findById(id)).thenReturn(Optional.of(execution));
        when(executions.save(execution)).thenReturn(execution);

        service.complete(id);

        assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
        verify(history).save(any());
        verify(outbox).save(any());
    }

    @Test
    void repeatedCompletionDoesNotCreateSecondEvent() {
        Execution execution = Execution.queue(10L, java.time.OffsetDateTime.now());
        execution.complete(java.time.OffsetDateTime.now());
        UUID id = execution.getId();
        when(executions.findById(id)).thenReturn(Optional.of(execution));

        service.complete(id);

        verify(outbox, never()).save(any());
        verify(history, never()).save(any());
    }

    @Test
    void missingExecutionFailsClearly() {
        UUID id = UUID.randomUUID();
        when(executions.findById(id)).thenReturn(Optional.empty());

        assertThrows(ExecutionNotFoundException.class, () -> service.complete(id));
    }

    @Test
    void authorizationCreatesInboxRecordOnce() {
        when(inbox.existsById(any())).thenReturn(false);
        when(executions.findByOsId(10L)).thenReturn(Optional.empty());
        when(executions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.authorize(10L, UUID.randomUUID());

        verify(inbox).save(any());
    }

    @Test
    void repeatedAuthorizationEventDoesNotCreateAnotherExecution() {
        UUID eventId = UUID.randomUUID();
        Execution execution = Execution.queue(10L, java.time.OffsetDateTime.now());
        when(inbox.existsById(eventId)).thenReturn(true);
        when(executions.findByOsId(10L)).thenReturn(Optional.of(execution));

        assertEquals(execution, service.authorize(10L, eventId));
        verify(inbox, never()).save(any());
    }

    @Test
    void invalidTransitionIsPropagated() {
        Execution execution = Execution.queue(10L, java.time.OffsetDateTime.now());
        UUID id = execution.getId();
        when(executions.findById(id)).thenReturn(Optional.of(execution));
        assertThrows(IllegalStateException.class, () -> service.startRepair(id));
    }

    @Test
    void newAuthorizationPersistsExecutionAndHistory() {
        when(inbox.existsById(any())).thenReturn(false);
        when(executions.findByOsId(10L)).thenReturn(Optional.empty());
        when(executions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.authorize(10L, UUID.randomUUID());

        verify(executions).save(any());
        verify(history).save(any());
    }

    @Test
    void diagnosisRepairCancelAndHistoryAreDelegated() {
        Execution execution = Execution.queue(10L, java.time.OffsetDateTime.now());
        UUID id = execution.getId();
        when(executions.findById(id)).thenReturn(Optional.of(execution));
        when(executions.save(execution)).thenReturn(execution);
        when(history.findByExecutionIdOrderByOccurredAtAsc(id)).thenReturn(List.of());

        service.startDiagnosis(id);
        service.startRepair(id);
        service.cancel(id);

        assertEquals(ExecutionStatus.CANCELLED, execution.getStatus());
        assertEquals(0, service.history(id).size());
        verify(history, times(3)).save(any());
    }
}
