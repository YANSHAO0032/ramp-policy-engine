package com.ramppolicy.engine.domain;

import java.time.Clock;
import java.time.Instant;

/**
 * Immutable evaluation input for deterministic policy execution.
 *
 * @param orderId order identifier
 * @param orderType order type
 * @param inputHash raw-line hash for audit correlation
 * @param policyVersion active policy version
 * @param order parsed order
 * @param evaluatedAt evaluation timestamp
 */
public record EvaluationContext(
        String orderId,
        OrderType orderType,
        String inputHash,
        String policyVersion,
        OrderRecord order,
        Instant evaluatedAt) {

    /**
     * Builds the evaluation context from a parsed order.
     *
     * @param order parsed order
     * @param inputHash raw-line hash for audit correlation
     * @param clock evaluation clock
     * @return evaluation context
     */
    public static EvaluationContext from(OrderRecord order, String inputHash, Clock clock) {
        return new EvaluationContext(
                order.orderId(),
                order.type(),
                inputHash,
                PolicyVersion.VALUE,
                order,
                clock.instant());
    }
}
