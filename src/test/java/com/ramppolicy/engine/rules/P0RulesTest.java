package com.ramppolicy.engine.rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ramppolicy.engine.domain.DeterministicDecision;
import com.ramppolicy.engine.domain.OrderRecord;
import com.ramppolicy.engine.domain.OrderType;
import com.ramppolicy.engine.facts.DemoFacts;
import com.ramppolicy.engine.io.JsonlOrderReader;
import com.ramppolicy.engine.policy.PolicyEngine;
import com.ramppolicy.engine.plan.StaticRulePlanResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class P0RulesTest {

    private DemoFacts facts;
    private JsonlOrderReader reader;
    private PolicyEngine engine;

    @BeforeEach
    void setUp() throws Exception {
        facts = DemoFacts.load(Path.of("src/main/resources/demo-data"), new ObjectMapper());
        reader = new JsonlOrderReader(new ObjectMapper());
        engine = new PolicyEngine(new StaticRulePlanResolver(), facts, Clock.fixed(Instant.parse("2026-07-28T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void customerStatusRulesHandleActiveAndInactiveCustomers() throws Exception {
        OrderRecord complete = reader.readOne(firstOrder("O-001"));
        OrderRecord hold = reader.readOne(firstOrder("O-010"));

        assertEquals(DeterministicDecision.class, engine.evaluate(complete).getClass());
        assertEquals("COMPLETE", engine.evaluate(complete).decision().name());
        assertEquals("COMPLIANCE_HOLD", engine.evaluate(hold).decision().name());
    }

    @Test
    void onRampAndOffRampRulesRejectWrongDirectionsAndBadQuotes() throws Exception {
        assertEquals("FREEZE", engine.evaluate(reader.readOne(firstOrder("O-003"))).decision().name());
        assertEquals("COMPLIANCE_HOLD", engine.evaluate(reader.readOne(firstOrder("O-004"))).decision().name());
        assertEquals("OPS_REVIEW", engine.evaluate(reader.readOne(firstOrder("O-005"))).decision().name());
        assertEquals("REQUOTE", engine.evaluate(reader.readOne(firstOrder("O-006"))).decision().name());
        assertEquals("OPS_REVIEW", engine.evaluate(reader.readOne(firstOrder("O-007"))).decision().name());
        assertEquals("OPS_REVIEW", engine.evaluate(reader.readOne(firstOrder("O-008"))).decision().name());
        assertEquals("TEMPORARY_HOLD", engine.evaluate(reader.readOne(firstOrder("O-009"))).decision().name());
        assertEquals("COMPLIANCE_HOLD", engine.evaluate(reader.readOne(firstOrder("O-010"))).decision().name());
        assertEquals("COMPLIANCE_HOLD", engine.evaluate(reader.readOne(firstOrder("O-011"))).decision().name());
        assertEquals("REJECT", engine.evaluate(reader.readOne(firstOrder("O-012"))).decision().name());
        assertEquals("COMPLIANCE_HOLD", engine.evaluate(reader.readOne(firstOrder("O-014"))).decision().name());
    }

    @Test
    void onRampConservationAndMinimumAmountAreEnforced() throws Exception {
        assertEquals("COMPLETE", engine.evaluate(reader.readOne(firstOrder("O-001"))).decision().name());
        assertEquals("COMPLETE", engine.evaluate(reader.readOne(firstOrder("O-002"))).decision().name());
    }

    private static String firstOrder(String orderId) throws Exception {
        List<String> lines = Files.readAllLines(Path.of("src/test/resources/demo-data/orders.jsonl"));
        return lines.stream()
                .filter(line -> line.contains("\"order_id\": \"" + orderId + "\""))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("missing order " + orderId));
    }
}
