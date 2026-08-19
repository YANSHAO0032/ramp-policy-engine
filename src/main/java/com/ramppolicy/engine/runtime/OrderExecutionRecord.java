package com.ramppolicy.engine.runtime;

import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.EscalationTarget;
import com.ramppolicy.engine.domain.ReasonCode;
import com.ramppolicy.engine.domain.Retryability;

import java.util.List;
import java.util.Set;

/**
 * One deterministic order execution outcome plus execution-gate metadata.
 *
 * @param orderId order identifier
 * @param decision final policy decision
 * @param reasonCodes structured reasons
 * @param escalationTargets escalation targets
 * @param retryability strictest retryability
 * @param actionExecuted whether the executor actually performed a funds action
 * @param actionType action type when executed
 * @param evidence evidence captured during evaluation
 */
public record OrderExecutionRecord(
        String orderId,
        Decision decision,
        Set<ReasonCode> reasonCodes,
        Set<EscalationTarget> escalationTargets,
        Retryability retryability,
        boolean actionExecuted,
        String actionType,
        List<String> evidence) {
}
