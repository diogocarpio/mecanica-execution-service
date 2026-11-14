package com.f1rsters.execution.controller;

import com.f1rsters.execution.exception.ExecutionNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void notFoundReturns404WithErrorMessage() {
        UUID executionId = UUID.randomUUID();
        ExecutionNotFoundException exception = new ExecutionNotFoundException(executionId);
        
        ResponseEntity<Map<String, String>> response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(handler.notFound(exception));
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertTrue(response.getBody().get("error").contains("Execucao nao encontrada"));
        assertTrue(response.getBody().get("error").contains(executionId.toString()));
    }

    @Test
    void conflictReturns409WithErrorMessage() {
        String errorMessage = "Invalid state transition";
        IllegalStateException exception = new IllegalStateException(errorMessage);
        
        ResponseEntity<Map<String, String>> response = ResponseEntity.status(HttpStatus.CONFLICT).body(handler.conflict(exception));
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals(errorMessage, response.getBody().get("error"));
    }
}
