package com.f1rsters.execution.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.f1rsters.execution.domain.Execution;
import com.f1rsters.execution.domain.ExecutionHistory;
import com.f1rsters.execution.domain.InboxProcessedMessage;
import com.f1rsters.execution.domain.OutboxEvent;
import com.f1rsters.execution.repository.ExecutionHistoryRepository;
import com.f1rsters.execution.repository.ExecutionRepository;
import com.f1rsters.execution.repository.InboxProcessedMessageRepository;
import com.f1rsters.execution.repository.OutboxEventRepository;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.E;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ExecutionSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExecutionRepository executions;

    @Autowired
    private ExecutionHistoryRepository history;

    @Autowired
    private OutboxEventRepository outbox;

    @Autowired
    private InboxProcessedMessageRepository inbox;

    private final Map<UUID, Execution> executionStore = new HashMap<>();
    private final Map<Long, Execution> executionByOs = new HashMap<>();
    private final Map<UUID, List<ExecutionHistory>> historyStore = new HashMap<>();
    private final List<OutboxEvent> outboxStore = new ArrayList<>();
    private UUID executionId;
    private MvcResult lastResponse;

    @Before
    public void resetScenario() {
        reset(executions, history, outbox, inbox);
        executionStore.clear();
        executionByOs.clear();
        historyStore.clear();
        outboxStore.clear();
        executionId = null;
        lastResponse = null;

        when(executions.findByOsId(anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(executionByOs.get(invocation.getArgument(0))));
        when(executions.findById(any(UUID.class)))
                .thenAnswer(invocation -> Optional.ofNullable(executionStore.get(invocation.getArgument(0))));
        when(executions.save(any(Execution.class))).thenAnswer(invocation -> {
            Execution execution = invocation.getArgument(0);
            executionStore.put(execution.getId(), execution);
            executionByOs.put(execution.getOsId(), execution);
            return execution;
        });
        when(history.save(any(ExecutionHistory.class))).thenAnswer(invocation -> {
            ExecutionHistory entry = invocation.getArgument(0);
            historyStore.computeIfAbsent(entry.getExecutionId(), ignored -> new ArrayList<>()).add(entry);
            return entry;
        });
        when(history.findByExecutionIdOrderByOccurredAtAsc(any(UUID.class)))
                .thenAnswer(invocation -> historyStore.getOrDefault(
                        invocation.getArgument(0), List.of()));
        when(outbox.save(any(OutboxEvent.class))).thenAnswer(invocation -> {
            OutboxEvent event = invocation.getArgument(0);
            outboxStore.add(event);
            return event;
        });
        when(inbox.existsById(any(UUID.class))).thenReturn(false);
        when(inbox.save(any(InboxProcessedMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Dado("que não existe execução para a OS {long}")
    public void queNaoExisteExecucao(Long osId) {
        assertThat(executionByOs).doesNotContainKey(osId);
    }

    @Quando("autorizo a OS {long}")
    public void autorizoOs(Long osId) throws Exception {
        lastResponse = mockMvc.perform(post("/api/executions/authorization")
                        .contentType("application/json")
                        .content("{\"osId\":" + osId + ",\"eventId\":\""
                                + UUID.randomUUID() + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        executionId = UUID.fromString(JsonSupport.read(lastResponse, "executionId"));
    }

    @E("inicio o diagnóstico")
    public void inicioDiagnostico() throws Exception {
        lastResponse = mockMvc.perform(post("/api/executions/" + executionId + "/diagnosis/start"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @E("inicio o reparo")
    public void inicioReparo() throws Exception {
        lastResponse = mockMvc.perform(post("/api/executions/" + executionId + "/repairs/start"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @E("concluo a execução")
    public void concluoExecucao() throws Exception {
        lastResponse = mockMvc.perform(post("/api/executions/" + executionId + "/complete"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Então("a execução deve estar no estado {string}")
    public void execucaoDeveEstarNoEstado(String expectedStatus) {
        assertThat(JsonSupport.read(lastResponse, "status")).isEqualTo(expectedStatus);
    }

    @Então("o histórico deve conter as transições {string}")
    public void historicoDeveConterTransicoes(String expectedTransitions) throws Exception {
        lastResponse = mockMvc.perform(get("/api/executions/" + executionId + "/history"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(JsonSupport.readHistoryStatuses(lastResponse))
                .containsExactlyElementsOf(List.of(expectedTransitions.split(",")));
    }

    @Então("deve existir exatamente um evento outbox do tipo {string}")
    public void deveExistirEventoOutbox(String eventType) {
        List<OutboxEvent> matchingEvents = outboxStore.stream()
                .filter(event -> event.getEventType().equals(eventType))
                .toList();
        assertThat(matchingEvents).hasSize(1);
        assertThat(matchingEvents.get(0).getAggregateId()).isEqualTo(executionId);
    }
}
