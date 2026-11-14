package com.f1rsters.execution.dto;

import java.time.OffsetDateTime;

public record HistoryResponse(String fromStatus, String toStatus, OffsetDateTime occurredAt) {
}
