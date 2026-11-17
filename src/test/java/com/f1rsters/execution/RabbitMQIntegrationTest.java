package com.f1rsters.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.f1rsters.execution.domain.Execution;
import com.f1rsters.execution.domain.ExecutionStatus;
import com.f1rsters.execution.domain.InboxProcessedMessage;
import com.f1rsters.execution.domain.OutboxEvent;
import com.f1rsters.execution.repository.ExecutionRepository;
import com.f1rsters.execution.repository.InboxProcessedMessageRepository;
import com.f1rsters.execution.repository.OutboxEventRepository;
import com.f1rsters.execution.service.ExecutionApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "RABBITMQ_TEST_ENABLED", matches = "true")
class RabbitMQIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ExecutionRepository executions;

    @Autowired
    private OutboxEventRepository outbox;

    @Autowired
    private InboxProcessedMessageRepository inbox;

    @Autowired
    private ExecutionApplicationService service;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        executions.deleteAll();
        outbox.deleteAll();
        inbox.deleteAll();
    }

    @Test
    void consumeApprovedMessageCreatesExecution() throws Exception {
        UUID eventId = UUID.randomUUID();
        Long osId = 500L;
        String body = String.format(
                "{\"eventId\":\"%s\",\"osId\":%d,\"clienteId\":\"cli1\",\"veiculoId\":\"veh1\","
                        + "\"servicosIds\":[\"srv1\"],\"pecasIds\":[\"p1\"],"
                        + "\"status\":\"APPROVED\",\"eventType\":\"os.approved\",\"timestamp\":\"2026-01-01T10:00:00Z\"}",
                eventId, osId);

        Message message = MessageBuilder.withBody(body.getBytes()).build();
        rabbitTemplate.send("os.approved", message);

        await().atMost(10, TimeUnit.SECONDS).until(() -> executions.findByOsId(osId).isPresent());

        Execution execution = executions.findByOsId(osId).orElseThrow();
        assertEquals(ExecutionStatus.QUEUED, execution.getStatus());
        assertEquals(osId, execution.getOsId());
        assertTrue(inbox.existsById(eventId));
    }

    @Test
    void consumeCancelledMessageCancelsExecution() throws Exception {
        UUID eventId = UUID.randomUUID();
        Long osId = 501L;
        
        Execution execution = executions.save(Execution.queue(osId, OffsetDateTime.now()));
        execution.startDiagnosis(OffsetDateTime.now());
        executions.save(execution);

        String body = String.format(
                "{\"eventId\":\"%s\",\"osId\":%d,\"clienteId\":\"cli1\",\"veiculoId\":\"veh1\","
                        + "\"servicosIds\":[\"srv1\"],\"pecasIds\":[\"p1\"],"
                        + "\"status\":\"CANCELLED\",\"eventType\":\"os.cancelled\",\"timestamp\":\"2026-01-01T10:00:00Z\"}",
                eventId, osId);

        Message message = MessageBuilder.withBody(body.getBytes()).build();
        rabbitTemplate.send("os.cancelled", message);

        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            Execution updated = executions.findById(execution.getId()).orElse(null);
            return updated != null && updated.getStatus() == ExecutionStatus.CANCELLED;
        });

        Execution cancelled = executions.findById(execution.getId()).orElseThrow();
        assertEquals(ExecutionStatus.CANCELLED, cancelled.getStatus());
        assertTrue(inbox.existsById(eventId));
    }

    @Test
    void duplicateMessageIsIdempotent() throws Exception {
        UUID eventId = UUID.randomUUID();
        Long osId = 502L;
        String body = String.format(
                "{\"eventId\":\"%s\",\"osId\":%d,\"clienteId\":\"cli1\",\"veiculoId\":\"veh1\","
                        + "\"servicosIds\":[\"srv1\"],\"pecasIds\":[\"p1\"],"
                        + "\"status\":\"APPROVED\",\"eventType\":\"os.approved\",\"timestamp\":\"2026-01-01T10:00:00Z\"}",
                eventId, osId);

        Message message = MessageBuilder.withBody(body.getBytes()).build();
        
        rabbitTemplate.send("os.approved", message);
        await().atMost(10, TimeUnit.SECONDS).until(() -> executions.findByOsId(osId).isPresent());

        Execution first = executions.findByOsId(osId).orElseThrow();
        UUID firstId = first.getId();

        rabbitTemplate.send("os.approved", message);
        Thread.sleep(1000);

        Execution second = executions.findByOsId(osId).orElseThrow();
        assertEquals(firstId, second.getId());
    }

    @Test
    void outboxEventIsPublishedToExchange() throws Exception {
        Execution execution = executions.save(Execution.queue(503L, OffsetDateTime.now()));
        execution.startDiagnosis(OffsetDateTime.now());
        execution.startRepair(OffsetDateTime.now());
        executions.save(execution);

        service.complete(execution.getId());

        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            List<OutboxEvent> events = outbox.findTop100ByPublishedFalseOrderByOccurredAtAsc();
            return events.isEmpty();
        });

        List<OutboxEvent> published = outbox.findTop100ByPublishedFalseOrderByOccurredAtAsc();
        assertTrue(published.isEmpty());
    }

    @Test
    void messageWithoutOsIdIsRejected() throws Exception {
        UUID eventId = UUID.randomUUID();
        String body = String.format(
                "{\"eventId\":\"%s\",\"osId\":null,\"clienteId\":\"cli1\",\"veiculoId\":\"veh1\","
                        + "\"servicosIds\":[\"srv1\"],\"pecasIds\":[\"p1\"],"
                        + "\"status\":\"APPROVED\",\"eventType\":\"os.approved\",\"timestamp\":\"2026-01-01T10:00:00Z\"}",
                eventId);

        Message message = MessageBuilder.withBody(body.getBytes()).build();
        rabbitTemplate.send("os.approved", message);

        Thread.sleep(2000);

        assertFalse(inbox.existsById(eventId));
    }
}
