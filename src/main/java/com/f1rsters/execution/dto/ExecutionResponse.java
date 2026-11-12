package com.f1rsters.execution.dto;

import java.util.UUID;

public record ExecutionResponse(UUID executionId, Long osId, String status) {
}
