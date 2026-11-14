package com.f1rsters.execution.repository;

import com.f1rsters.execution.domain.ExecutionHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.UUID;

public interface ExecutionHistoryRepository extends MongoRepository<ExecutionHistory, UUID> {
    List<ExecutionHistory> findByExecutionIdOrderByOccurredAtAsc(UUID executionId);
}
