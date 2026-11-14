package com.f1rsters.execution.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExecutionTest {

    @Test
    void deveCriarExecucaoNaFila() {
        Execution execution = Execution.queue(10L, OffsetDateTime.parse("2026-01-01T10:00:00Z"));

        assertEquals(10L, execution.getOsId());
        assertEquals(ExecutionStatus.QUEUED, execution.getStatus());
    }

    @Test
    void deveConcluirUmaExecucaoUmaUnicaVez() {
        Execution execution = Execution.queue(10L, OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        execution.complete(OffsetDateTime.parse("2026-01-01T11:00:00Z"));
        execution.complete(OffsetDateTime.parse("2026-01-01T12:00:00Z"));

        assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
        assertEquals(OffsetDateTime.parse("2026-01-01T11:00:00Z"), execution.getUpdatedAt());
    }

    @Test
    void naoDeveConcluirExecucaoCancelada() {
        Execution execution = Execution.queue(10L, OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        execution.cancel(OffsetDateTime.parse("2026-01-01T10:30:00Z"));

        assertThrows(IllegalStateException.class, () -> {
            execution.complete(OffsetDateTime.parse("2026-01-01T11:00:00Z"));
        });
    }

    @Test
    void devePercorrerDiagnosticoEReparo() {
        Execution execution = Execution.queue(10L, OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        execution.startDiagnosis(OffsetDateTime.parse("2026-01-01T10:01:00Z"));
        execution.startRepair(OffsetDateTime.parse("2026-01-01T10:02:00Z"));

        assertEquals(ExecutionStatus.IN_REPAIR, execution.getStatus());
    }

    @Test
    void cancelamentoRepetidoEhIdempotente() {
        Execution execution = Execution.queue(10L, OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        execution.cancel(OffsetDateTime.parse("2026-01-01T10:01:00Z"));
        execution.cancel(OffsetDateTime.parse("2026-01-01T10:02:00Z"));

        assertEquals(ExecutionStatus.CANCELLED, execution.getStatus());
        assertEquals(OffsetDateTime.parse("2026-01-01T10:01:00Z"), execution.getUpdatedAt());
    }

    @Test
    void naoDeveCancelarExecucaoConcluida() {
        Execution execution = Execution.queue(10L, OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        execution.complete(OffsetDateTime.parse("2026-01-01T10:01:00Z"));

        assertThrows(IllegalStateException.class,
                () -> execution.cancel(OffsetDateTime.parse("2026-01-01T10:02:00Z")));
    }

    @Test
    void deveTestarGetters() {
        Execution execution = Execution.queue(10L, OffsetDateTime.parse("2026-01-01T10:00:00Z"));
        execution.startDiagnosis(OffsetDateTime.parse("2026-01-01T10:01:00Z"));
        execution.complete(OffsetDateTime.parse("2026-01-01T10:02:00Z"));

        assertEquals(10L, execution.getOsId());
        assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
        assertEquals(OffsetDateTime.parse("2026-01-01T10:00:00Z"), execution.getCreatedAt());
        assertEquals(OffsetDateTime.parse("2026-01-01T10:02:00Z"), execution.getUpdatedAt());
        assertEquals(OffsetDateTime.parse("2026-01-01T10:00:00Z"), execution.getApprovedAt());
        assertNotNull(execution.getId());
    }
}
