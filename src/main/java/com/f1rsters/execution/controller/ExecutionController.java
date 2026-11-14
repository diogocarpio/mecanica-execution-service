package com.f1rsters.execution.controller;

import com.f1rsters.execution.service.ExecutionApplicationService;
import com.f1rsters.execution.dto.AuthorizationRequest;
import com.f1rsters.execution.dto.ExecutionResponse;
import com.f1rsters.execution.dto.HistoryResponse;
import com.f1rsters.execution.mapper.ExecutionMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

    private final ExecutionApplicationService service;
    private final ExecutionMapper mapper;

    public ExecutionController(ExecutionApplicationService service, ExecutionMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping("/authorization")
    @ResponseStatus(HttpStatus.CREATED)
    public ExecutionResponse authorize(@Valid @RequestBody AuthorizationRequest request) {
        return mapper.toResponse(service.authorize(request.osId(), request.eventId()));
    }

    @GetMapping("/queue")
    public List<ExecutionResponse> queue() {
        return service.queue().stream().map(mapper::toResponse).toList();
    }

    @PostMapping("/{executionId}/complete")
    public ExecutionResponse complete(@PathVariable UUID executionId) {
        return mapper.toResponse(service.complete(executionId));
    }

    @PostMapping("/{executionId}/diagnosis/start")
    public ExecutionResponse startDiagnosis(@PathVariable UUID executionId) {
        return mapper.toResponse(service.startDiagnosis(executionId));
    }

    @PostMapping("/{executionId}/repairs/start")
    public ExecutionResponse startRepair(@PathVariable UUID executionId) {
        return mapper.toResponse(service.startRepair(executionId));
    }

    @PostMapping("/{executionId}/cancel")
    public ExecutionResponse cancel(@PathVariable UUID executionId) {
        return mapper.toResponse(service.cancel(executionId));
    }

    @GetMapping("/{executionId}/history")
    public List<HistoryResponse> history(@PathVariable UUID executionId) {
        return service.history(executionId).stream().map(mapper::toResponse).toList();
    }
}
