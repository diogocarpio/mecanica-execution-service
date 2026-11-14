package com.f1rsters.execution.validation;

import com.f1rsters.execution.dto.OSEvent;
import com.f1rsters.execution.exception.SagaContractException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OSEventValidatorTest {

    private final OSEventValidator validator = new OSEventValidator();

    @Test
    void requireOsIdDoesNotThrowWhenOsIdIsPresent() {
        OSEvent event = new OSEvent(
                UUID.randomUUID(),
                123L,
                "cliente1",
                "veiculo1",
                List.of("servico1"),
                List.of("peca1"),
                BigDecimal.valueOf(100.0),
                "APPROVED",
                "os.approved",
                OffsetDateTime.now()
        );
        
        assertDoesNotThrow(() -> validator.requireOsId(event, "os.approved"));
    }

    @Test
    void requireOsIdThrowsWhenOsIdIsNull() {
        OSEvent event = new OSEvent(
                UUID.randomUUID(),
                null,
                "cliente1",
                "veiculo1",
                List.of("servico1"),
                List.of("peca1"),
                BigDecimal.valueOf(100.0),
                "APPROVED",
                "os.approved",
                OffsetDateTime.now()
        );
        
        SagaContractException exception = assertThrows(
                SagaContractException.class,
                () -> validator.requireOsId(event, "os.approved")
        );
        
        assertTrue(exception.getMessage().contains("osId obrigatorio"));
        assertTrue(exception.getMessage().contains("os.approved"));
    }
}
