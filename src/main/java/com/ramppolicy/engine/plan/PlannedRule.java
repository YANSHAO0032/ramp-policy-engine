package com.ramppolicy.engine.plan;

/**
 * A policy rule selected for an order type.
 *
 * @param ruleId rule identifier
 * @param mandatory whether this rule must pass for a COMPLETE candidate
 */
public record PlannedRule(RuleId ruleId, boolean mandatory) {
}
