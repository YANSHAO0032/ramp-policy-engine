package com.ramppolicy.engine.plan;

/**
 * 某一订单类型被选中的策略规则。
 *
 * @param ruleId 规则标识
 * @param mandatory 是否必须通过才允许成为 COMPLETE 候选
 */
public record PlannedRule(RuleId ruleId, boolean mandatory) {
}
