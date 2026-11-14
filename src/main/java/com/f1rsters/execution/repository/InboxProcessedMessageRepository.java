package com.f1rsters.execution.repository;

import com.f1rsters.execution.domain.InboxProcessedMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface InboxProcessedMessageRepository extends MongoRepository<InboxProcessedMessage, UUID> {
}
