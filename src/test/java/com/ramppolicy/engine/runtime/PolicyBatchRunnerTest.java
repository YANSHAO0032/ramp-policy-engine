package com.ramppolicy.engine.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.ReasonCode;
import com.ramppolicy.engine.domain.OrderRecord;
import com.ramppolicy.engine.facts.DemoFacts;
import com.ramppolicy.engine.io.JsonlOrderReader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyBatchRunnerTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-07-28T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void demoBatchWritesResultsAndAuditFiles() throws Exception {
        Path outputDir = Files.createTempDirectory("ramp-policy-output");
        PolicyBatchRunner runner = newRunner();

        BatchRunResult result = runner.runDemo(outputDir);

        assertEquals(14, result.results().size());
        assertTrue(Files.exists(outputDir.resolve("results.json")));
        assertTrue(Files.exists(outputDir.resolve("audit.jsonl")));

        OrderExecutionRecord o013 = result.results().stream()
                .filter(record -> "O-013".equals(record.orderId()))
                .findFirst()
                .orElseThrow();
        assertEquals(Decision.OPS_REVIEW, o013.decision());
        assertTrue(o013.reasonCodes().contains(ReasonCode.DUPLICATE_TRANSACTION));
        assertFalse(o013.actionExecuted());
    }

    @Test
    void duplicateOrderIntakeIsHeldWithoutAction() throws Exception {
        Path orders = Files.createTempFile("duplicate-orders", ".jsonl");
        Files.writeString(orders, """
                {"order_id": "O-001", "type": "on_ramp", "customer_id": "c001", "asset": "USDT", "network": "ERC20", "fiat_amount_usd": 1000, "quoted_crypto_amount": 1000, "quote_expires_at": "2026-07-28T12:05:00Z", "fiat_status": "received", "destination_address": "0xCLEAN01", "customer_note": ""}
                {"order_id": "O-001", "type": "on_ramp", "customer_id": "c001", "asset": "USDT", "network": "ERC20", "fiat_amount_usd": 1000, "quoted_crypto_amount": 1000, "quote_expires_at": "2026-07-28T12:05:00Z", "fiat_status": "received", "destination_address": "0xCLEAN01", "customer_note": ""}
                """.trim(), StandardCharsets.UTF_8);
        Path outputDir = Files.createTempDirectory("duplicate-orders-output");
        PolicyBatchRunner runner = newRunner();

        BatchRunResult result = runner.run(orders, outputDir);

        assertEquals(2, result.results().size());
        assertEquals(Decision.COMPLETE, result.results().get(0).decision());
        assertEquals(Decision.OPS_REVIEW, result.results().get(1).decision());
        assertTrue(result.results().get(1).reasonCodes().contains(ReasonCode.DUPLICATE_ORDER));
        assertFalse(result.results().get(1).actionExecuted());
    }

    private static PolicyBatchRunner newRunner() throws Exception {
        DemoFacts facts = DemoFacts.loadFromClasspath(Path.of("demo-data"), new ObjectMapper());
        return new PolicyBatchRunner(
                facts,
                new JsonlOrderReader(new ObjectMapper()),
                new ObjectMapper(),
                FIXED_CLOCK);
    }
}
