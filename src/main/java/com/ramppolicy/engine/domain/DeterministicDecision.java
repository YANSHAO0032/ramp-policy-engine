package com.ramppolicy.engine.domain;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Deterministic policy output with all reasons preserved.
 *
 * @param orderId order identifier
 * @param decision top-level decision
 * @param reasonCodes structured reasons
 * @param escalationTargets escalation targets
 * @param retryability retryability classification
 * @param evidence evidence items
 * @param policyVersion active policy version
 * @param evaluatedAt evaluation timestamp
 */
public record DeterministicDecision(
        String orderId,
        Decision decision,
        Set<ReasonCode> reasonCodes,
        Set<EscalationTarget> escalationTargets,
        Retryability retryability,
        List<String> evidence,
        String policyVersion,
        Instant evaluatedAt) {
}
