package com.ramppolicy.engine.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.ReasonCode;
import com.ramppolicy.engine.facts.DemoFacts;
import com.ramppolicy.engine.infrastructure.llm.ExplanationProviderFactory;
import com.ramppolicy.engine.infrastructure.llm.ExplanationProviderType;
import com.ramppolicy.engine.infrastructure.llm.LlmProperties;
import com.ramppolicy.engine.io.JsonlOrderReader;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfflineNoKeyIntegrationTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-07-28T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void noLlmEnvironmentRunsFourteenOrdersWithStubExplanations() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        DemoFacts facts = DemoFacts.loadFromClasspath(Path.of("demo-data"), objectMapper);
        PolicyBatchRunner runner = new PolicyBatchRunner(
                facts,
                new JsonlOrderReader(objectMapper),
                objectMapper,
                FIXED_CLOCK,
                new ExplanationProviderFactory(LlmProperties.from(Map.of())).create());

        BatchRunResult result = runner.runDemo(Files.createTempDirectory("offline-no-key-output"));

        assertEquals(14, result.results().size());
        assertEquals(ExplanationProviderType.STUB, result.results().get(0).explanationProvider());
        assertGolden(result);

        OrderExecutionRecord o014 = byOrder(result).get("O-014");
        assertEquals(Decision.COMPLIANCE_HOLD, o014.decision());
        assertTrue(o014.reasonCodes().contains(ReasonCode.ADDRESS_HIGH_RISK));
        assertFalse(o014.actionExecuted());
        assertTrue(o014.explanation().contains("COMPLIANCE_HOLD"));
    }

    private static Map<String, OrderExecutionRecord> byOrder(BatchRunResult result) {
        return result.results().stream().collect(Collectors.toMap(OrderExecutionRecord::orderId, record -> record));
    }

    private static void assertGolden(BatchRunResult result) {
        Map<String, Decision> expected = Map.ofEntries(
                Map.entry("O-001", Decision.COMPLETE),
                Map.entry("O-002", Decision.COMPLETE),
                Map.entry("O-003", Decision.FREEZE),
                Map.entry("O-004", Decision.COMPLIANCE_HOLD),
                Map.entry("O-005", Decision.OPS_REVIEW),
                Map.entry("O-006", Decision.REQUOTE),
                Map.entry("O-007", Decision.OPS_REVIEW),
                Map.entry("O-008", Decision.OPS_REVIEW),
                Map.entry("O-009", Decision.TEMPORARY_HOLD),
                Map.entry("O-010", Decision.COMPLIANCE_HOLD),
                Map.entry("O-011", Decision.COMPLIANCE_HOLD),
                Map.entry("O-012", Decision.REJECT),
                Map.entry("O-013", Decision.OPS_REVIEW),
                Map.entry("O-014", Decision.COMPLIANCE_HOLD));
        Map<String, OrderExecutionRecord> actual = byOrder(result);
        expected.forEach((orderId, decision) -> assertEquals(decision, actual.get(orderId).decision(), orderId));
    }
}
