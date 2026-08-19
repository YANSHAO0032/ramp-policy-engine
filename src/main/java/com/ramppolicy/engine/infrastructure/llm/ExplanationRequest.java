package com.ramppolicy.engine.infrastructure.llm;

import com.ramppolicy.engine.domain.DeterministicDecision;

/**
 * 传给解释器的已验证输入。
 *
 * @param orderId 订单标识
 * @param decision 确定性决策，永远不由模型控制
 */
public record ExplanationRequest(
        String orderId,
        DeterministicDecision decision) {
}
