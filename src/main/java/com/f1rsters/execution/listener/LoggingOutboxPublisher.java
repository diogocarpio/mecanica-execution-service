package com.f1rsters.execution.listener;

import com.f1rsters.execution.servide.OutboxPublisher;
import com.f1rsters.execution.domain.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(name = "BROKER_ENABLED", havingValue = "true")
public class LoggingOutboxPublisher implements OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(LoggingOutboxPublisher.class);

    @Override
    public void publish(OutboxEvent event) {
        log.info("Outbox event pending broker adapter: id={}, type={}, payload={}",
                event.getId(), event.getEventType(), event.getPayload());
    }
}
