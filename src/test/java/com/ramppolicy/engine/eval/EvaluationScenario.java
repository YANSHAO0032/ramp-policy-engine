package com.ramppolicy.engine.eval;

import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.ReasonCode;
import com.ramppolicy.engine.domain.OrderRecord;
import com.ramppolicy.engine.facts.DemoFacts;

import java.util.Set;

/**
 * 评测场景。
 *
 * @param id 场景标识
 * @param order 订单
 * @param facts 场景对应事实
 * @param expectedDecision 期望决策，未指定时为空
 * @param expectedReasonCodes 期望原因码，未指定时为空
 * @param expectedActionExecuted 期望是否执行动作，未指定时为空
 * @param tags 场景标签
 */
public record EvaluationScenario(
        String id,
        OrderRecord order,
        DemoFacts facts,
        Decision expectedDecision,
        Set<ReasonCode> expectedReasonCodes,
        Boolean expectedActionExecuted,
        Set<String> tags) {

    public static EvaluationScenario expected(
            String id,
            OrderRecord order,
            DemoFacts facts,
            Decision expectedDecision,
            Set<ReasonCode> expectedReasonCodes,
            boolean expectedActionExecuted,
            Set<String> tags) {
        return new EvaluationScenario(id, order, facts, expectedDecision, expectedReasonCodes, expectedActionExecuted, tags);
    }

    public static EvaluationScenario generated(
            String id,
            OrderRecord order,
            DemoFacts facts,
            Set<String> tags) {
        return new EvaluationScenario(id, order, facts, null, Set.of(), null, tags);
    }
}
