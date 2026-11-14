package com.f1rsters.execution.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.StreamSupport;
import org.springframework.test.web.servlet.MvcResult;

final class JsonSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonSupport() {
    }

    static String read(MvcResult result, String field) {
        try {
            return MAPPER.readTree(result.getResponse().getContentAsString()).get(field).asText();
        } catch (Exception exception) {
            throw new AssertionError("Não foi possível ler o campo JSON " + field, exception);
        }
    }

    static List<String> readHistoryStatuses(MvcResult result) {
        try {
            JsonNode root = MAPPER.readTree(result.getResponse().getContentAsString());
            return StreamSupport.stream(root.spliterator(), false)
                    .map(node -> node.get("toStatus").asText())
                    .toList();
        } catch (Exception exception) {
            throw new AssertionError("Não foi possível ler o histórico JSON", exception);
        }
    }
}
