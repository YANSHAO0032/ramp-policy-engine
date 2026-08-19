package com.ramppolicy.engine.rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.DeterministicDecision;
import com.ramppolicy.engine.domain.OrderRecord;
import com.ramppolicy.engine.domain.OrderType;
import com.ramppolicy.engine.facts.DemoFacts;
import com.ramppolicy.engine.policy.PolicyEngine;
import com.ramppolicy.engine.plan.StaticRulePlanResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessRuleBoundaryTest {

    private PolicyEngine engine;

    @BeforeEach
    void setUp() throws Exception {
        DemoFacts facts = DemoFacts.load(Path.of("src/main/resources/demo-data"), new ObjectMapper());
        engine = new PolicyEngine(new StaticRulePlanResolver(), facts, Clock.fixed(Instant.parse("2026-07-28T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void kycLimitEqualPassesOnRamp() {
        DeterministicDecision decision = engine.evaluate(onRamp("B-kyc-eq", new BigDecimal("50000"), new BigDecimal("50000"), Instant.parse("2026-07-28T12:05:00Z")));

        assertEquals(Decision.COMPLETE, decision.decision());
    }

    @Test
    void minimumAmountEqualPassesOnRamp() {
        DeterministicDecision decision = engine.evaluate(onRamp("B-min-eq", new BigDecimal("20"), new BigDecimal("20"), Instant.parse("2026-07-28T12:05:00Z")));

        assertEquals(Decision.COMPLETE, decision.decision());
    }

    @Test
    void quoteSlippageAtOnePercentPassesButAboveOnePercentRequotes() {
        DeterministicDecision pass = engine.evaluate(onRampBtc("B-slip-pass", new BigDecimal("67670"), new BigDecimal("1.0"), Instant.parse("2026-07-28T12:00:00Z")));
        DeterministicDecision requote = engine.evaluate(onRampBtc("B-slip-fail", new BigDecimal("67740"), new BigDecimal("1.0"), Instant.parse("2026-07-28T12:00:00Z")));

        assertEquals(Decision.COMPLETE, pass.decision());
        assertEquals(Decision.REQUOTE, requote.decision());
    }

    private static OrderRecord onRamp(String orderId, BigDecimal fiatAmountUsd, BigDecimal quotedCryptoAmount, Instant quoteExpiresAt) {
        return new OrderRecord(
                orderId,
                OrderType.ON_RAMP,
                "c001",
                "USDT",
                "ERC20",
                fiatAmountUsd,
                quotedCryptoAmount,
                quoteExpiresAt,
                "received",
                "0xCLEAN01",
                null,
                null,
                null,
                null,
                "");
    }

    private static OrderRecord onRampBtc(String orderId, BigDecimal fiatAmountUsd, BigDecimal quotedCryptoAmount, Instant quoteExpiresAt) {
        return new OrderRecord(
                orderId,
                OrderType.ON_RAMP,
                "c003",
                "BTC",
                "BTC",
                fiatAmountUsd,
                quotedCryptoAmount,
                quoteExpiresAt,
                "received",
                "0xCLEAN02",
                null,
                null,
                null,
                null,
                "");
    }
}
