package com.ramppolicy.engine.eval;

import java.math.BigDecimal;

/**
 * Agent 评测汇总报告。
 *
 * @param commitSha 当前评测对应的 Git 提交
 * @param seed 随机种子
 * @param generatedOrders 10k 生成批次的订单数
 * @param processedOrders 10k 生成批次的已处理订单数
 * @param goldenCases Golden 回归用例数
 * @param goldenPassCount Golden 通过数
 * @param boundaryCases 边界用例数
 * @param boundaryPassCount 边界通过数
 * @param conflictCases 冲突用例数
 * @param conflictPassCount 冲突通过数
 * @param safetyInvariantChecks 安全不变量检查次数
 * @param safetyInvariantViolations 安全不变量违规次数
 * @param unauthorizedPayouts 未授权资金动作次数
 * @param duplicatePayouts 重复资金动作次数
 * @param promptInjectionCases 提示注入用例数
 * @param promptInjectionBypasses 提示注入绕过次数
 * @param toolFailureCases 工具失败用例数
 * @param unsafeFailOpenCases 不安全 fail-open 次数
 * @param llmVariationCalls LLM 变体调用次数
 * @param decisionDriftCount 决策漂移次数
 * @param reasonCodeDriftCount 原因码漂移次数
 * @param actionDriftCount 动作漂移次数
 * @param realPaidLlmCalls 真实付费 LLM 调用次数
 * @param totalDurationMs 总耗时毫秒数
 * @param averageLatencyMs 平均单条耗时毫秒数
 * @param passed 是否通过
 */
public record EvaluationReport(
        String commitSha,
        long seed,
        int generatedOrders,
        int processedOrders,
        int goldenCases,
        int goldenPassCount,
        int boundaryCases,
        int boundaryPassCount,
        int conflictCases,
        int conflictPassCount,
        long safetyInvariantChecks,
        long safetyInvariantViolations,
        long unauthorizedPayouts,
        long duplicatePayouts,
        long promptInjectionCases,
        long promptInjectionBypasses,
        long toolFailureCases,
        long unsafeFailOpenCases,
        long llmVariationCalls,
        long decisionDriftCount,
        long reasonCodeDriftCount,
        long actionDriftCount,
        long realPaidLlmCalls,
        long totalDurationMs,
        BigDecimal averageLatencyMs,
        boolean passed) {
}
