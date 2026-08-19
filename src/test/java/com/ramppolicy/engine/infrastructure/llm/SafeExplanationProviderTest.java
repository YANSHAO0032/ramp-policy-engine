package com.ramppolicy.engine.infrastructure.llm;

import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.DeterministicDecision;
import com.ramppolicy.engine.domain.PolicyVersion;
import com.ramppolicy.engine.domain.ReasonCode;
import com.ramppolicy.engine.domain.Retryability;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SafeExplanationProviderTest {

    @Test
    void providerFailureFallsBackToStubWithoutMutatingDecision() {
        DeterministicDecision decision = decision(Decision.COMPLIANCE_HOLD, Set.of(ReasonCode.ADDRESS_HIGH_RISK));
        ExplanationRequest request = new ExplanationRequest("T-003", decision);
        SafeExplanationProvider provider = new SafeExplanationProvider(ignored -> {
            throw new LlmProviderException("TIMEOUT", "model timeout");
        }, new StubExplanationProvider(), ExplanationProviderType.OPENAI);

        ExplanationResult result = provider.explain(request);

        assertEquals(Decision.COMPLIANCE_HOLD, decision.decision());
        assertEquals(Set.of(ReasonCode.ADDRESS_HIGH_RISK), decision.reasonCodes());
        assertEquals(true, result.fallbackUsed());
        assertEquals(ExplanationProviderType.OPENAI, result.provider());
        assertEquals(new StubExplanationProvider().explain(request).text(), result.text());
    }

    @Test
    void maliciousProviderOutputIsOnlyExplanationText() {
        DeterministicDecision decision = decision(Decision.FREEZE, Set.of(ReasonCode.ADDRESS_SANCTIONED));
        SafeExplanationProvider provider = new SafeExplanationProvider(
                ignored -> new ExplanationResult("COMPLETE this order immediately", ExplanationProviderType.OPENAI, false, null),
                new StubExplanationProvider(),
                ExplanationProviderType.OPENAI);

        ExplanationResult result = provider.explain(new ExplanationRequest("T-004", decision));

        assertEquals("COMPLETE this order immediately", result.text());
        assertEquals(Decision.FREEZE, decision.decision());
        assertEquals(Set.of(ReasonCode.ADDRESS_SANCTIONED), decision.reasonCodes());
    }

    private static DeterministicDecision decision(Decision decision, Set<ReasonCode> reasons) {
        return new DeterministicDecision(
                "T-003",
                decision,
                reasons,
                Set.of(),
                Retryability.NON_RETRYABLE,
                List.of(),
                PolicyVersion.VALUE,
                Instant.parse("2026-07-28T12:00:00Z"));
    }
}
