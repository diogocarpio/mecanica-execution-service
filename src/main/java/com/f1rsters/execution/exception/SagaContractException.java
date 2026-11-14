package com.f1rsters.execution.exception;

public class SagaContractException extends RuntimeException {
    public SagaContractException(String message) {
        super(message);
    }

    public SagaContractException(String message, Throwable cause) {
        super(message, cause);
    }
}
