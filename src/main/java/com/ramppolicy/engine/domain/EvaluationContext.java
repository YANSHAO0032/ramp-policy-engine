package com.ramppolicy.engine.domain;

import java.time.Clock;
import java.time.Instant;

/**
 * 确定性策略执行使用的不可变评估上下文。
 *
 * @param orderId 订单标识
 * @param orderType 订单类型
 * @param inputHash 原始输入行哈希，用于审计关联
 * @param policyVersion 生效策略版本
 * @param order 已解析订单
 * @param evaluatedAt 评估时间
 */
public record EvaluationContext(
        String orderId,
        OrderType orderType,
        String inputHash,
        String policyVersion,
        OrderRecord order,
        Instant evaluatedAt) {

    /**
     * 根据已解析订单构造评估上下文。
     *
     * @param order 已解析订单
     * @param inputHash 原始输入行哈希
     * @param clock 评估时钟
     * @return 评估上下文
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
