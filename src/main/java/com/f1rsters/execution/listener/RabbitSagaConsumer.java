package com.f1rsters.execution.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.f1rsters.execution.servide.ExecutionApplicationService;
import com.f1rsters.execution.config.MessagingConfig;
import com.f1rsters.execution.dto.OSEvent;
import com.f1rsters.execution.exception.SagaContractException;
import com.f1rsters.execution.validation.OSEventValidator;
import com.f1rsters.execution.util.EventIdUtil;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "BROKER_ENABLED", havingValue = "true")
public class RabbitSagaConsumer {
    private final ObjectMapper objectMapper;
    private final ExecutionApplicationService service;
    private final OSEventValidator validator;

    public RabbitSagaConsumer(ObjectMapper objectMapper, ExecutionApplicationService service,
                              OSEventValidator validator) {
        this.objectMapper = objectMapper;
        this.service = service;
        this.validator = validator;
    }

    @RabbitListener(queues = MessagingConfig.APPROVED_QUEUE)
    public void authorize(String body) throws Exception {
        OSEvent message = objectMapper.readValue(body, OSEvent.class);
        validator.requireOsId(message, "os.approved");
        UUID eventId = EventIdUtil.resolve(message.eventId(), body);
        service.authorize(message.osId(), eventId);
    }

    @RabbitListener(queues = MessagingConfig.CANCEL_QUEUE)
    public void cancel(String body) throws Exception {
        OSEvent message = objectMapper.readValue(body, OSEvent.class);
        validator.requireOsId(message, "os.cancelled");
        UUID eventId = EventIdUtil.resolve(message.eventId(), body);
        service.cancelByOsId(message.osId(), eventId);
    }
}
