package com.f1rsters.execution.mapper;

import com.f1rsters.execution.domain.Execution;
import com.f1rsters.execution.domain.ExecutionHistory;
import com.f1rsters.execution.dto.ExecutionResponse;
import com.f1rsters.execution.dto.HistoryResponse;
import org.springframework.stereotype.Component;

@Component
public class ExecutionMapper {
    public ExecutionResponse toResponse(Execution execution) {
        return new ExecutionResponse(execution.getId(), execution.getOsId(), execution.getStatus().name());
    }

    public HistoryResponse toResponse(ExecutionHistory history) {
        return new HistoryResponse(history.getFromStatus(), history.getToStatus(), history.getOccurredAt());
    }
}
