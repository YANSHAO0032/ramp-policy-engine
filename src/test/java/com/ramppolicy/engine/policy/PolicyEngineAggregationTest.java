package com.ramppolicy.engine.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.DeterministicDecision;
import com.ramppolicy.engine.domain.OrderRecord;
import com.ramppolicy.engine.facts.DemoFacts;
import com.ramppolicy.engine.io.JsonlOrderReader;
import com.ramppolicy.engine.plan.StaticRulePlanResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyEngineAggregationTest {

    private PolicyEngine engine;
    private JsonlOrderReader reader;

    @BeforeEach
    void setUp() throws Exception {
        DemoFacts facts = DemoFacts.load(Path.of("src/main/resources/demo-data"), new ObjectMapper());
        engine = new PolicyEngine(new StaticRulePlanResolver(), facts, Clock.fixed(Instant.parse("2026-07-28T12:00:00Z"), ZoneOffset.UTC));
        reader = new JsonlOrderReader(new ObjectMapper());
    }

    @Test
    void amountMismatchAndPayoutConservationReasonsAreMerged() throws Exception {
        DeterministicDecision decision = engine.evaluate(read("O-005"));

        assertEquals(Decision.OPS_REVIEW, decision.decision());
        assertTrue(decision.reasonCodes().containsAll(List.of(
                com.ramppolicy.engine.domain.ReasonCode.AMOUNT_MISMATCH,
                com.ramppolicy.engine.domain.ReasonCode.PAYOUT_EXCEEDS_CONFIRMED_VALUE)));
    }

    @Test
    void bankMismatchEscalatesToCompliance() throws Exception {
        DeterministicDecision decision = engine.evaluate(read("O-012"));

        assertEquals(Decision.REJECT, decision.decision());
        assertTrue(decision.escalationTargets().contains(com.ramppolicy.engine.domain.EscalationTarget.COMPLIANCE));
        assertTrue(decision.reasonCodes().contains(com.ramppolicy.engine.domain.ReasonCode.BANK_NAME_MISMATCH));
    }

    @Test
    void travelRuleAndWithdrawalFundsReasonsAreBothRetained() throws Exception {
        DeterministicDecision decision = engine.evaluate(read("O-011"));

        assertEquals(Decision.COMPLIANCE_HOLD, decision.decision());
        assertTrue(decision.reasonCodes().contains(com.ramppolicy.engine.domain.ReasonCode.TRAVEL_RULE_INFO_MISSING));
        assertTrue(decision.reasonCodes().contains(com.ramppolicy.engine.domain.ReasonCode.WITHDRAWAL_FUNDS_UNVERIFIED));
    }

    private OrderRecord read(String orderId) throws Exception {
        List<String> lines = Files.readAllLines(Path.of("src/test/resources/demo-data/orders.jsonl"));
        String json = lines.stream()
                .filter(line -> line.contains("\"order_id\": \"" + orderId + "\""))
                .findFirst()
                .orElseThrow();
        return reader.readOne(json);
    }
}
