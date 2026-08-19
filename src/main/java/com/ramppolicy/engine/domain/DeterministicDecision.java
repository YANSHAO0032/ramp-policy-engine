package com.ramppolicy.engine.domain;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * 策略引擎输出的确定性决策，保留所有命中原因和证据。
 *
 * @param orderId 订单标识
 * @param decision 顶层处置结果
 * @param reasonCodes 结构化原因码集合
 * @param escalationTargets 需要升级的复核团队集合
 * @param retryability 自动重试语义
 * @param evidence 规则命中的证据文本
 * @param policyVersion 生效策略版本
 * @param evaluatedAt 评估时间
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
