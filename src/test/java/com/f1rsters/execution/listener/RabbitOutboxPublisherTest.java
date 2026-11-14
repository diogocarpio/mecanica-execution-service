package com.f1rsters.execution.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.f1rsters.execution.config.MessagingConfig;
import com.f1rsters.execution.domain.OutboxEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitOutboxPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void publishSendsEventToRabbit() throws Exception {
        RabbitOutboxPublisher publisher = new RabbitOutboxPublisher(rabbitTemplate, objectMapper);
        
        UUID aggregateId = UUID.randomUUID();
        String eventType = "execution.completed";
        String payload = "{\"test\":\"data\"}";
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        OutboxEvent event = new OutboxEvent(aggregateId, eventType, payload, occurredAt);
        
        ObjectNode jsonNode = mock(ObjectNode.class);
        when(objectMapper.readTree(payload)).thenReturn(jsonNode);
        
        publisher.publish(event);
        
        verify(rabbitTemplate).convertAndSend(
                eq(MessagingConfig.EXCHANGE),
                eq(eventType),
                eq(jsonNode)
        );
    }

    @Test
    void publishThrowsIllegalStateExceptionOnJsonError() throws Exception {
        RabbitOutboxPublisher publisher = new RabbitOutboxPublisher(rabbitTemplate, objectMapper);
        
        UUID aggregateId = UUID.randomUUID();
        String eventType = "execution.completed";
        String payload = "invalid json";
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        OutboxEvent event = new OutboxEvent(aggregateId, eventType, payload, occurredAt);
        
        when(objectMapper.readTree(payload)).thenThrow(new JsonProcessingException("Invalid JSON") {});
        
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> publisher.publish(event)
        );
        
        assertTrue(exception.getMessage().contains("Falha ao serializar evento de outbox"));
        verifyNoInteractions(rabbitTemplate);
    }
}
