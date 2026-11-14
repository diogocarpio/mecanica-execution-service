package com.f1rsters.execution.bdd;

import static org.mockito.Mockito.mock;

import com.f1rsters.execution.repository.ExecutionHistoryRepository;
import com.f1rsters.execution.repository.ExecutionRepository;
import com.f1rsters.execution.repository.InboxProcessedMessageRepository;
import com.f1rsters.execution.repository.OutboxEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class BddTestConfiguration {

    @Bean
    @Primary
    ExecutionRepository executionRepository() {
        return mock(ExecutionRepository.class);
    }

    @Bean
    @Primary
    ExecutionHistoryRepository executionHistoryRepository() {
        return mock(ExecutionHistoryRepository.class);
    }

    @Bean
    @Primary
    OutboxEventRepository outboxEventRepository() {
        return mock(OutboxEventRepository.class);
    }

    @Bean
    @Primary
    InboxProcessedMessageRepository inboxProcessedMessageRepository() {
        return mock(InboxProcessedMessageRepository.class);
    }

    @Bean
    @Primary
    Clock bddClock() {
        return Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
    }
}
