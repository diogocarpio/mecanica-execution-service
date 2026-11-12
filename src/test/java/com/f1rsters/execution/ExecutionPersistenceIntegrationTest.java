package com.f1rsters.execution;

import com.f1rsters.execution.repository.ExecutionRepository;
import com.f1rsters.execution.repository.OutboxEventRepository;
import com.f1rsters.execution.domain.Execution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "MONGODB_TEST_ENABLED", matches = "true")
class ExecutionPersistenceIntegrationTest {
    @Autowired
    private ExecutionRepository executions;

    @Autowired
    private OutboxEventRepository outbox;

    @Test
    void repositoriesAreAvailableWithIsolatedDatabase() {
        Execution execution = executions.save(Execution.queue(99L, java.time.OffsetDateTime.now()));

        assertTrue(executions.findByOsId(99L).isPresent());
        assertTrue(outbox.findTop100ByPublishedFalseOrderByOccurredAtAsc().isEmpty());
        executions.delete(execution);
    }
}
