package com.f1rsters.execution.exception;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void executionNotFoundExceptionHasCorrectMessage() {
        UUID executionId = UUID.randomUUID();
        ExecutionNotFoundException exception = new ExecutionNotFoundException(executionId);
        
        assertTrue(exception.getMessage().contains("Execucao nao encontrada"));
        assertTrue(exception.getMessage().contains(executionId.toString()));
    }

    @Test
    void executionNotFoundExceptionIsRuntimeException() {
        UUID executionId = UUID.randomUUID();
        ExecutionNotFoundException exception = new ExecutionNotFoundException(executionId);
        
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void sagaContractExceptionWithMessage() {
        String message = "Contract violation";
        SagaContractException exception = new SagaContractException(message);
        
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void sagaContractExceptionWithMessageAndCause() {
        String message = "Contract violation";
        Throwable cause = new RuntimeException("Root cause");
        SagaContractException exception = new SagaContractException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }
}
