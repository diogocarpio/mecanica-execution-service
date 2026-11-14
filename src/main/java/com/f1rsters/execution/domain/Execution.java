package com.f1rsters.execution.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.UUID;

@Document("executions")
@CompoundIndex(name = "queue_order_idx", def = "{'status': 1, 'approvedAt': 1}")
public class Execution {

    @Id
    private UUID id;

    @Indexed(unique = true)
    private Long osId;

    private ExecutionStatus status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    private OffsetDateTime approvedAt;

    @Version
    private long version;

    protected Execution() {
    }

    private Execution(UUID id, Long osId, OffsetDateTime now) {
        this.id = id;
        this.osId = osId;
        this.status = ExecutionStatus.QUEUED;
        this.createdAt = now;
        this.updatedAt = now;
        this.approvedAt = now;
    }

    public static Execution queue(Long osId, OffsetDateTime now) {
        return new Execution(UUID.randomUUID(), osId, now);
    }

    public void complete(OffsetDateTime now) {
        if (status == ExecutionStatus.CANCELLED) {
            throw new IllegalStateException("Execucao cancelada nao pode ser concluida");
        }
        if (status == ExecutionStatus.COMPLETED) {
            return;
        }
        status = ExecutionStatus.COMPLETED;
        updatedAt = now;
    }

    public void startDiagnosis(OffsetDateTime now) {
        requireStatus(ExecutionStatus.QUEUED);
        status = ExecutionStatus.IN_DIAGNOSIS;
        updatedAt = now;
    }

    public void startRepair(OffsetDateTime now) {
        requireStatus(ExecutionStatus.IN_DIAGNOSIS);
        status = ExecutionStatus.IN_REPAIR;
        updatedAt = now;
    }

    public void cancel(OffsetDateTime now) {
        if (status == ExecutionStatus.COMPLETED) {
            throw new IllegalStateException("Execucao concluida nao pode ser cancelada");
        }
        if (status == ExecutionStatus.CANCELLED) {
            return;
        }
        status = ExecutionStatus.CANCELLED;
        updatedAt = now;
    }

    private void requireStatus(ExecutionStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Transicao invalida: estado atual " + status);
        }
    }

    public UUID getId() { return id; }
    public Long getOsId() { return osId; }
    public ExecutionStatus getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
}
