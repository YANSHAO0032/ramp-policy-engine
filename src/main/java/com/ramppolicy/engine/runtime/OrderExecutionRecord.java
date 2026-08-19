package com.ramppolicy.engine.runtime;

import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.EscalationTarget;
import com.ramppolicy.engine.domain.ReasonCode;
import com.ramppolicy.engine.domain.Retryability;
import com.ramppolicy.engine.infrastructure.llm.ExplanationProviderType;

import java.util.List;
import java.util.Set;

/**
 * 单个订单的确定性执行结果及执行门元数据。
 *
 * @param orderId 订单标识
 * @param decision 最终策略结果
 * @param reasonCodes 结构化原因码
 * @param escalationTargets 升级复核团队
 * @param retryability 最严格的自动重试语义
 * @param actionExecuted 是否真的执行了资金动作
 * @param actionType 已执行时的动作类型
 * @param evidence 评估阶段采集的证据
 * @param explanation 非权威的纯文本解释
 * @param explanationProvider 使用的解释器类型
 * @param explanationFallbackUsed 是否使用了 fallback 解释
 */
public record OrderExecutionRecord(
        String orderId,
        Decision decision,
        Set<ReasonCode> reasonCodes,
        Set<EscalationTarget> escalationTargets,
        Retryability retryability,
        boolean actionExecuted,
        String actionType,
        List<String> evidence,
        String explanation,
        ExplanationProviderType explanationProvider,
        boolean explanationFallbackUsed) {
}
