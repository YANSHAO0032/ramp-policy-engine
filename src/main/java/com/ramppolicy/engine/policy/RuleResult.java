package com.ramppolicy.engine.policy;

import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.EscalationTarget;
import com.ramppolicy.engine.domain.ReasonCode;
import com.ramppolicy.engine.domain.Retryability;
import com.ramppolicy.engine.plan.RuleId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Deterministic result of a single planned rule.
 *
 * @param ruleId rule that produced the result
 * @param outcome pass/warn/block outcome
 * @param blockingDecision decision when the rule blocks
 * @param reasonCodes structured reasons
 * @param escalationTargets escalation targets
 * @param retryability retryability classification
 * @param evidence evidence strings
 */
public record RuleResult(
        RuleId ruleId,
        RuleOutcome outcome,
        Optional<Decision> blockingDecision,
        Set<ReasonCode> reasonCodes,
        Set<EscalationTarget> escalationTargets,
        Retryability retryability,
        List<String> evidence) {

    /**
     * Creates a passing rule result with no reasons or evidence.
     *
     * @param ruleId rule identifier
     * @return passing result
     */
    public static RuleResult pass(RuleId ruleId) {
        return new RuleResult(ruleId, RuleOutcome.PASS, Optional.empty(), Set.of(), Set.of(), Retryability.NOT_APPLICABLE, List.of());
    }

    /**
     * Creates an advisory rule result.
     *
     * @param ruleId rule identifier
     * @param reasons structured reasons
     * @param escalations escalation targets
     * @param evidence evidence strings
     * @return warning result
     */
    public static RuleResult warn(RuleId ruleId, Set<ReasonCode> reasons, Set<EscalationTarget> escalations, List<String> evidence) {
        return new RuleResult(ruleId, RuleOutcome.WARN, Optional.empty(), reasons, escalations, Retryability.NOT_APPLICABLE, evidence);
    }

    /**
     * Creates a blocking rule result.
     *
     * @param ruleId rule identifier
     * @param decision blocking decision
     * @param reasons structured reasons
     * @param escalations escalation targets
     * @param retryability retryability classification
     * @param evidence evidence strings
     * @return blocking result
     */
    public static RuleResult block(
            RuleId ruleId,
            Decision decision,
            Set<ReasonCode> reasons,
            Set<EscalationTarget> escalations,
            Retryability retryability,
            List<String> evidence) {
        return new RuleResult(ruleId, RuleOutcome.BLOCK, Optional.of(decision), reasons, escalations, retryability, evidence);
    }
}
