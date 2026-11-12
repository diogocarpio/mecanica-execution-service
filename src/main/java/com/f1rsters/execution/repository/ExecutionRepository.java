package com.f1rsters.execution.repository;

import com.f1rsters.execution.domain.Execution;
import com.f1rsters.execution.domain.ExecutionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutionRepository extends MongoRepository<Execution, UUID> {
    Optional<Execution> findByOsId(Long osId);
    List<Execution> findTop100ByStatusOrderByApprovedAtAsc(ExecutionStatus status);
}
