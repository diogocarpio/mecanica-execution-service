package com.f1rsters.execution.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.f1rsters.execution.service.OutboxPublisher;
import com.f1rsters.execution.config.MessagingConfig;
import com.f1rsters.execution.domain.OutboxEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "BROKER_ENABLED", havingValue = "true")
public class RabbitOutboxPublisher implements OutboxPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RabbitOutboxPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(OutboxEvent event) {
        try {
            rabbitTemplate.convertAndSend(MessagingConfig.EXCHANGE, event.getEventType(),
                    objectMapper.readTree(event.getPayload()));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Falha ao serializar evento de outbox", exception);
        }
    }
}
