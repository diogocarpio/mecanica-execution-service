package com.f1rsters.execution.listener;

import com.f1rsters.execution.repository.OutboxEventRepository;
import com.f1rsters.execution.servide.OutboxPublisher;
import com.f1rsters.execution.domain.OutboxEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(name = "BROKER_ENABLED", havingValue = "true")
public class OutboxPublisherJob {
    private final OutboxEventRepository repository;
    private final OutboxPublisher publisher;

    public OutboxPublisherJob(OutboxEventRepository repository, OutboxPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelayString = "${OUTBOX_PUBLISH_DELAY_MS:5000}")
    public void publishPending() {
        for (OutboxEvent event : repository.findTop100ByPublishedFalseOrderByOccurredAtAsc()) {
            publisher.publish(event);
            event.markPublished();
            repository.save(event);
        }
    }
}
