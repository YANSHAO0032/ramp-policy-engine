package com.ramppolicy.engine.plan;

import com.ramppolicy.engine.domain.OrderType;

import java.util.List;
import java.util.Set;

/**
 * 某一订单类型对应的规则计划和事实计划。
 *
 * @param orderType 订单类型
 * @param policyVersion 生效策略版本
 * @param rules 仅包含对该订单类型适用的规则
 * @param requiredFacts 这些规则所需的事实集合
 */
public record RulePlan(
        OrderType orderType,
        String policyVersion,
        List<PlannedRule> rules,
        Set<FactRequirement> requiredFacts) {
}
