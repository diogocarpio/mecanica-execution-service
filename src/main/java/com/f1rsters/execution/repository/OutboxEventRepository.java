package com.f1rsters.execution.repository;

import com.f1rsters.execution.domain.OutboxEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends MongoRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop100ByPublishedFalseOrderByOccurredAtAsc();
}
