package com.ramppolicy.engine.infrastructure.llm;

import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.DeterministicDecision;
import com.ramppolicy.engine.domain.EscalationTarget;
import com.ramppolicy.engine.domain.PolicyVersion;
import com.ramppolicy.engine.domain.ReasonCode;
import com.ramppolicy.engine.domain.Retryability;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StubExplanationProviderTest {

    @Test
    void outputIsStableForEveryDecision() {
        StubExplanationProvider provider = new StubExplanationProvider();

        for (Decision decision : Decision.values()) {
            ExplanationResult first = provider.explain(request(decision, Set.of()));
            for (int i = 0; i < 100; i++) {
                assertEquals(first, provider.explain(request(decision, Set.of())));
            }
        }
    }

    @Test
    void reasonCodeOrderIsStable() {
        StubExplanationProvider provider = new StubExplanationProvider();
        ExplanationRequest first = request(Decision.OPS_REVIEW, EnumSet.of(ReasonCode.PAYOUT_EXCEEDS_CONFIRMED_VALUE, ReasonCode.AMOUNT_MISMATCH));
        ExplanationRequest second = request(Decision.OPS_REVIEW, EnumSet.of(ReasonCode.AMOUNT_MISMATCH, ReasonCode.PAYOUT_EXCEEDS_CONFIRMED_VALUE));

        assertEquals(provider.explain(first), provider.explain(second));
    }

    private static ExplanationRequest request(Decision decision, Set<ReasonCode> reasons) {
        DeterministicDecision deterministicDecision = new DeterministicDecision(
                "T-001",
                decision,
                reasons,
                Set.of(EscalationTarget.OPS),
                Retryability.NON_RETRYABLE,
                List.of(),
                PolicyVersion.VALUE,
                Instant.parse("2026-07-28T12:00:00Z"));
        return new ExplanationRequest("T-001", deterministicDecision);
    }
}
