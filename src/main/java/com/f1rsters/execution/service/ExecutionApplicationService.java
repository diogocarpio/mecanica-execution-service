package com.f1rsters.execution.service;

import com.f1rsters.execution.repository.ExecutionRepository;
import com.f1rsters.execution.repository.ExecutionHistoryRepository;
import com.f1rsters.execution.repository.OutboxEventRepository;
import com.f1rsters.execution.repository.InboxProcessedMessageRepository;
import com.f1rsters.execution.domain.Execution;
import com.f1rsters.execution.domain.ExecutionHistory;
import com.f1rsters.execution.domain.ExecutionStatus;
import com.f1rsters.execution.domain.OutboxEvent;
import com.f1rsters.execution.exception.ExecutionNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Service
public class ExecutionApplicationService {

    private final ExecutionRepository repository;
    private final ExecutionHistoryRepository historyRepository;
    private final OutboxEventRepository outboxRepository;
    private final InboxProcessedMessageRepository inboxRepository;
    private final Clock clock;

    public ExecutionApplicationService(ExecutionRepository repository,
                                      ExecutionHistoryRepository historyRepository,
                                      OutboxEventRepository outboxRepository,
                                      InboxProcessedMessageRepository inboxRepository,
                                      Clock clock) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.outboxRepository = outboxRepository;
        this.inboxRepository = inboxRepository;
        this.clock = clock;
    }

    public Execution authorize(Long osId) {
        return repository.findByOsId(osId).orElseGet(() -> {
            OffsetDateTime now = OffsetDateTime.now(clock);
            Execution execution = repository.save(Execution.queue(osId, now));
            historyRepository.save(new ExecutionHistory(execution.getId(), null, ExecutionStatus.QUEUED, now));
            return execution;
        });
    }

    public Execution authorize(Long osId, UUID eventId) {
        if (eventId == null) {
            throw new IllegalArgumentException("eventId obrigatorio para deduplicacao");
        }
        if (inboxRepository.existsById(eventId)) {
            return repository.findByOsId(osId).orElseThrow(() ->
                    new IllegalStateException("Mensagem processada sem execucao correspondente"));
        }
        Execution execution = authorize(osId);
        inboxRepository.save(new com.f1rsters.execution.domain.InboxProcessedMessage(
                eventId, "os.approved", OffsetDateTime.now(clock)));
        return execution;
    }

    public Optional<Execution> cancelByOsId(Long osId, UUID eventId) {
        if (eventId == null) {
            throw new IllegalArgumentException("eventId obrigatorio para deduplicacao");
        }
        if (inboxRepository.existsById(eventId)) {
            return repository.findByOsId(osId);
        }
        Optional<Execution> execution = repository.findByOsId(osId);
        execution.ifPresent(value -> cancel(value.getId()));
        inboxRepository.save(new com.f1rsters.execution.domain.InboxProcessedMessage(
                eventId, "os.cancelled", OffsetDateTime.now(clock)));
        return execution;
    }

    public List<Execution> queue() {
        return repository.findTop100ByStatusOrderByApprovedAtAsc(ExecutionStatus.QUEUED);
    }

    public Optional<Execution> findById(UUID executionId) {
        return repository.findById(executionId);
    }

    public Optional<Execution> findByOsId(Long osId) {
        return repository.findByOsId(osId);
    }

    public Execution complete(UUID executionId) {
        Execution execution = get(executionId);
        if (execution.getStatus() == ExecutionStatus.COMPLETED) {
            return execution;
        }
        ExecutionStatus previous = execution.getStatus();
        OffsetDateTime now = OffsetDateTime.now(clock);
        execution.complete(OffsetDateTime.now(clock));
        Execution saved = repository.save(execution);
        historyRepository.save(new ExecutionHistory(executionId, previous, saved.getStatus(), now));
        outboxRepository.save(new OutboxEvent(executionId, "execution.completed",
                eventPayload(saved, "execution.completed", now), now));
        return saved;
    }

    public Execution startDiagnosis(UUID executionId) { return transition(executionId, ExecutionStatus.IN_DIAGNOSIS); }

    public Execution startRepair(UUID executionId) { return transition(executionId, ExecutionStatus.IN_REPAIR); }

    public Execution cancel(UUID executionId) {
        Execution execution = get(executionId);
        ExecutionStatus previous = execution.getStatus();
        OffsetDateTime now = OffsetDateTime.now(clock);
        execution.cancel(now);
        Execution saved = repository.save(execution);
        historyRepository.save(new ExecutionHistory(executionId, previous, saved.getStatus(), now));
        outboxRepository.save(new OutboxEvent(executionId, "os.status.updated",
                eventPayload(saved, "os.status.updated", now), now));
        return saved;
    }

    public java.util.List<ExecutionHistory> history(UUID executionId) {
        get(executionId);
        return historyRepository.findByExecutionIdOrderByOccurredAtAsc(executionId);
    }

    private Execution transition(UUID id, ExecutionStatus target) {
        Execution execution = get(id);
        ExecutionStatus previous = execution.getStatus();
        OffsetDateTime now = OffsetDateTime.now(clock);
        if (target == ExecutionStatus.IN_DIAGNOSIS) execution.startDiagnosis(now);
        else execution.startRepair(now);
        Execution saved = repository.save(execution);
        historyRepository.save(new ExecutionHistory(id, previous, target, now));
        outboxRepository.save(new OutboxEvent(id, "os.status.updated",
                eventPayload(saved, "os.status.updated", now), now));
        return saved;
    }

    private String eventPayload(Execution execution, String eventType, OffsetDateTime timestamp) {
        return "{\"eventId\":\"" + UUID.randomUUID() + "\",\"osId\":" + execution.getOsId()
                + ",\"clienteId\":null,\"veiculoId\":null,\"servicosIds\":[],\"pecasIds\":[],"
                + "\"valorTotal\":0,\"status\":\"" + externalStatus(execution.getStatus())
                + "\",\"eventType\":\"" + eventType + "\",\"timestamp\":\"" + timestamp + "\"}";
    }

    private String externalStatus(ExecutionStatus status) {
        return switch (status) {
            case IN_DIAGNOSIS -> "DIAGNOSTICO";
            case IN_REPAIR -> "EM_EXECUCAO";
            case COMPLETED -> "FINALIZADA";
            case CANCELLED -> "CANCELADA";
            case QUEUED -> "EM_EXECUCAO";
        };
    }

    private Execution get(UUID executionId) {
        return repository.findById(executionId).orElseThrow(() -> new ExecutionNotFoundException(executionId));
    }
}
