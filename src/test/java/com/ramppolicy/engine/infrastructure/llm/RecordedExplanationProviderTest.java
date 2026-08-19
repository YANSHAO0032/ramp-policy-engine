package com.ramppolicy.engine.infrastructure.llm;

import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.DeterministicDecision;
import com.ramppolicy.engine.domain.PolicyVersion;
import com.ramppolicy.engine.domain.ReasonCode;
import com.ramppolicy.engine.domain.Retryability;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordedExplanationProviderTest {

    @Test
    void recordedHitReturnsLocalText() {
        RecordedExplanationProvider provider = new RecordedExplanationProvider(
                Map.of("FREEZE|ADDRESS_SANCTIONED", "recorded freeze text"),
                new StubExplanationProvider());

        ExplanationResult result = provider.explain(request(Decision.FREEZE, Set.of(ReasonCode.ADDRESS_SANCTIONED)));

        assertEquals("recorded freeze text", result.text());
        assertEquals(ExplanationProviderType.RECORDED, result.provider());
        assertEquals(false, result.fallbackUsed());
    }

    @Test
    void recordedMissFallsBackToStub() {
        RecordedExplanationProvider provider = new RecordedExplanationProvider(Map.of(), new StubExplanationProvider());

        ExplanationResult result = provider.explain(request(Decision.REJECT, Set.of(ReasonCode.BANK_NAME_MISMATCH)));

        assertEquals(ExplanationProviderType.RECORDED, result.provider());
        assertEquals(true, result.fallbackUsed());
        assertEquals(new StubExplanationProvider().explain(request(Decision.REJECT, Set.of(ReasonCode.BANK_NAME_MISMATCH))).text(), result.text());
    }

    private static ExplanationRequest request(Decision decision, Set<ReasonCode> reasons) {
        DeterministicDecision deterministicDecision = new DeterministicDecision(
                "T-002",
                decision,
                reasons,
                Set.of(),
                Retryability.NON_RETRYABLE,
                List.of(),
                PolicyVersion.VALUE,
                Instant.parse("2026-07-28T12:00:00Z"));
        return new ExplanationRequest("T-002", deterministicDecision);
    }
}
