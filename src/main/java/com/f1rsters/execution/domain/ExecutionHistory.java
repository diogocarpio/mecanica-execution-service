package com.f1rsters.execution.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.UUID;

@Document("execution_history")
public class ExecutionHistory {
    @Id
    private UUID id;
    private UUID executionId;
    private String fromStatus;
    private String toStatus;
    private OffsetDateTime occurredAt;

    protected ExecutionHistory() {}

    public ExecutionHistory(UUID executionId, ExecutionStatus from, ExecutionStatus to, OffsetDateTime occurredAt) {
        this.id = UUID.randomUUID();
        this.executionId = executionId;
        this.fromStatus = from == null ? null : from.name();
        this.toStatus = to.name();
        this.occurredAt = occurredAt;
    }

    public UUID getId() { return id; }
    public UUID getExecutionId() { return executionId; }
    public String getFromStatus() { return fromStatus; }
    public String getToStatus() { return toStatus; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
}
