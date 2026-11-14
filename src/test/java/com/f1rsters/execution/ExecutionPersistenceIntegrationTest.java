package com.f1rsters.execution;

import com.f1rsters.execution.domain.Execution;
import com.f1rsters.execution.domain.ExecutionHistory;
import com.f1rsters.execution.domain.ExecutionStatus;
import com.f1rsters.execution.domain.InboxProcessedMessage;
import com.f1rsters.execution.domain.OutboxEvent;
import com.f1rsters.execution.repository.ExecutionHistoryRepository;
import com.f1rsters.execution.repository.ExecutionRepository;
import com.f1rsters.execution.repository.InboxProcessedMessageRepository;
import com.f1rsters.execution.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "MONGODB_TEST_ENABLED", matches = "true")
class ExecutionPersistenceIntegrationTest {
    @Autowired
    private ExecutionRepository executions;

    @Autowired
    private OutboxEventRepository outbox;

    @Autowired
    private ExecutionHistoryRepository history;

    @Autowired
    private InboxProcessedMessageRepository inbox;

    @BeforeEach
    void cleanDatabase() {
        executions.deleteAll();
        outbox.deleteAll();
        history.deleteAll();
        inbox.deleteAll();
    }

    @Test
    void repositoriesAreAvailableWithIsolatedDatabase() {
        Execution execution = executions.save(Execution.queue(99L, java.time.OffsetDateTime.now()));

        assertTrue(executions.findByOsId(99L).isPresent());
        assertTrue(outbox.findTop100ByPublishedFalseOrderByOccurredAtAsc().isEmpty());
        executions.delete(execution);
    }

    @Test
    void executionLifecyclePersistsCorrectly() {
        Execution execution = executions.save(Execution.queue(100L, OffsetDateTime.now()));
        UUID id = execution.getId();

        execution.startDiagnosis(OffsetDateTime.now());
        executions.save(execution);

        execution.startRepair(OffsetDateTime.now());
        executions.save(execution);

        execution.complete(OffsetDateTime.now());
        Execution completed = executions.save(execution);

        Execution retrieved = executions.findById(id).orElseThrow();
        assertEquals(ExecutionStatus.COMPLETED, retrieved.getStatus());
        assertEquals(100L, retrieved.getOsId());
    }

    @Test
    void historyPersistsWithExecution() {
        Execution execution = executions.save(Execution.queue(101L, OffsetDateTime.now()));
        UUID id = execution.getId();

        ExecutionHistory h1 = new ExecutionHistory(id, null, ExecutionStatus.QUEUED, OffsetDateTime.now());
        history.save(h1);

        execution.startDiagnosis(OffsetDateTime.now());
        executions.save(execution);

        ExecutionHistory h2 = new ExecutionHistory(id, ExecutionStatus.QUEUED, ExecutionStatus.IN_DIAGNOSIS, OffsetDateTime.now());
        history.save(h2);

        List<ExecutionHistory> histories = history.findByExecutionIdOrderByOccurredAtAsc(id);
        assertEquals(2, histories.size());
        assertEquals(ExecutionStatus.QUEUED, histories.get(0).getToStatus());
        assertEquals(ExecutionStatus.IN_DIAGNOSIS, histories.get(1).getToStatus());
    }

    @Test
    void outboxEventPersistsAndCanBeQueried() {
        UUID aggregateId = UUID.randomUUID();
        OutboxEvent event = new OutboxEvent(aggregateId, "test.event", "{\"data\":\"value\"}", OffsetDateTime.now());
        outbox.save(event);

        List<OutboxEvent> unpublished = outbox.findTop100ByPublishedFalseOrderByOccurredAtAsc();
        assertEquals(1, unpublished.size());
        assertEquals(aggregateId, unpublished.get(0).getAggregateId());
        assertFalse(unpublished.get(0).isPublished());

        event.markPublished();
        outbox.save(event);

        List<OutboxEvent> afterPublish = outbox.findTop100ByPublishedFalseOrderByOccurredAtAsc();
        assertTrue(afterPublish.isEmpty());
    }

    @Test
    void inboxProcessedMessagePreventsDuplicates() {
        UUID eventId = UUID.randomUUID();
        InboxProcessedMessage message = new InboxProcessedMessage(eventId, "os.approved", OffsetDateTime.now());
        inbox.save(message);

        assertTrue(inbox.existsById(eventId));

        InboxProcessedMessage retrieved = inbox.findById(eventId).orElseThrow();
        assertEquals("os.approved", retrieved.getEventType());
    }

    @Test
    void findByOsIdReturnsCorrectExecution() {
        executions.save(Execution.queue(200L, OffsetDateTime.now()));
        executions.save(Execution.queue(201L, OffsetDateTime.now()));

        assertTrue(executions.findByOsId(200L).isPresent());
        assertTrue(executions.findByOsId(201L).isPresent());
        assertFalse(executions.findByOsId(999L).isPresent());
    }

    @Test
    void findTop100ByStatusReturnsQueuedExecutions() {
        executions.save(Execution.queue(300L, OffsetDateTime.now()));
        Execution exec2 = executions.save(Execution.queue(301L, OffsetDateTime.now()));
        exec2.startDiagnosis(OffsetDateTime.now());
        executions.save(exec2);
        executions.save(Execution.queue(302L, OffsetDateTime.now()));

        List<Execution> queued = executions.findTop100ByStatusOrderByApprovedAtAsc(ExecutionStatus.QUEUED);
        assertEquals(2, queued.size());
        assertTrue(queued.stream().allMatch(e -> e.getStatus() == ExecutionStatus.QUEUED));
    }
}
