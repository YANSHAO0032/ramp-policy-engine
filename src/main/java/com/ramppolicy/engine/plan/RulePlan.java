package com.ramppolicy.engine.plan;

import com.ramppolicy.engine.domain.OrderType;

import java.util.List;
import java.util.Set;

/**
 * Rule and fact plan for one order type.
 *
 * @param orderType order type
 * @param policyVersion active policy version
 * @param rules applicable rules only
 * @param requiredFacts facts required by the selected rules
 */
public record RulePlan(
        OrderType orderType,
        String policyVersion,
        List<PlannedRule> rules,
        Set<FactRequirement> requiredFacts) {
}
