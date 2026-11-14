package com.f1rsters.execution.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.f1rsters.execution.domain.Execution;
import com.f1rsters.execution.dto.OSEvent;
import com.f1rsters.execution.exception.SagaContractException;
import com.f1rsters.execution.service.ExecutionApplicationService;
import com.f1rsters.execution.validation.OSEventValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitSagaConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ExecutionApplicationService service;

    @Mock
    private OSEventValidator validator;

    @Test
    void authorizeProcessesValidMessage() throws Exception {
        RabbitSagaConsumer consumer = new RabbitSagaConsumer(objectMapper, service, validator);
        
        UUID eventId = UUID.randomUUID();
        Long osId = 123L;
        String body = "{\"eventId\":\"" + eventId + "\",\"osId\":" + osId + ",\"eventType\":\"os.approved\"}";
        
        OSEvent event = new OSEvent(
                eventId, osId, "cliente1", "veiculo1",
                List.of("servico1"), List.of("peca1"),
                BigDecimal.valueOf(100.0), "APPROVED",
                "os.approved", OffsetDateTime.now()
        );
        
        Execution execution = Execution.queue(osId, OffsetDateTime.now());
        
        when(objectMapper.readValue(body, OSEvent.class)).thenReturn(event);
        when(service.authorize(osId, eventId)).thenReturn(execution);
        
        consumer.authorize(body);
        
        verify(validator).requireOsId(event, "os.approved");
        verify(service).authorize(osId, eventId);
    }

    @Test
    void authorizeThrowsOnMissingOsId() throws Exception {
        RabbitSagaConsumer consumer = new RabbitSagaConsumer(objectMapper, service, validator);
        
        UUID eventId = UUID.randomUUID();
        String body = "{\"eventId\":\"" + eventId + "\",\"osId\":null,\"eventType\":\"os.approved\"}";
        
        OSEvent event = new OSEvent(
                eventId, null, "cliente1", "veiculo1",
                List.of("servico1"), List.of("peca1"),
                BigDecimal.valueOf(100.0), "APPROVED",
                "os.approved", OffsetDateTime.now()
        );
        
        when(objectMapper.readValue(body, OSEvent.class)).thenReturn(event);
        doThrow(new SagaContractException("osId obrigatorio"))
                .when(validator).requireOsId(event, "os.approved");
        
        assertThrows(SagaContractException.class, () -> consumer.authorize(body));
        
        verify(validator).requireOsId(event, "os.approved");
        verify(service, never()).authorize(any(), any());
    }

    @Test
    void cancelProcessesValidMessage() throws Exception {
        RabbitSagaConsumer consumer = new RabbitSagaConsumer(objectMapper, service, validator);
        
        UUID eventId = UUID.randomUUID();
        Long osId = 123L;
        String body = "{\"eventId\":\"" + eventId + "\",\"osId\":" + osId + ",\"eventType\":\"os.cancelled\"}";
        
        OSEvent event = new OSEvent(
                eventId, osId, "cliente1", "veiculo1",
                List.of("servico1"), List.of("peca1"),
                BigDecimal.valueOf(100.0), "CANCELLED",
                "os.cancelled", OffsetDateTime.now()
        );
        
        Execution execution = Execution.queue(osId, OffsetDateTime.now());
        
        when(objectMapper.readValue(body, OSEvent.class)).thenReturn(event);
        when(service.cancelByOsId(osId, eventId)).thenReturn(Optional.of(execution));
        
        consumer.cancel(body);
        
        verify(validator).requireOsId(event, "os.cancelled");
        verify(service).cancelByOsId(osId, eventId);
    }

    @Test
    void cancelThrowsOnMissingOsId() throws Exception {
        RabbitSagaConsumer consumer = new RabbitSagaConsumer(objectMapper, service, validator);
        
        UUID eventId = UUID.randomUUID();
        String body = "{\"eventId\":\"" + eventId + "\",\"osId\":null,\"eventType\":\"os.cancelled\"}";
        
        OSEvent event = new OSEvent(
                eventId, null, "cliente1", "veiculo1",
                List.of("servico1"), List.of("peca1"),
                BigDecimal.valueOf(100.0), "CANCELLED",
                "os.cancelled", OffsetDateTime.now()
        );
        
        when(objectMapper.readValue(body, OSEvent.class)).thenReturn(event);
        doThrow(new SagaContractException("osId obrigatorio"))
                .when(validator).requireOsId(event, "os.cancelled");
        
        assertThrows(SagaContractException.class, () -> consumer.cancel(body));
        
        verify(validator).requireOsId(event, "os.cancelled");
        verify(service, never()).cancelByOsId(any(), any());
    }
}
