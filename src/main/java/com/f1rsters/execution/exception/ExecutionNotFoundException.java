package com.f1rsters.execution.exception;

import java.util.UUID;

public class ExecutionNotFoundException extends RuntimeException {
    public ExecutionNotFoundException(UUID executionId) {
        super("Execucao nao encontrada: " + executionId);
    }
}
