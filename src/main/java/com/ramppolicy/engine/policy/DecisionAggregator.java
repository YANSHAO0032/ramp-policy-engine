package com.ramppolicy.engine.policy;

import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.EscalationTarget;
import com.ramppolicy.engine.domain.ReasonCode;
import com.ramppolicy.engine.domain.Retryability;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 将多条规则结果聚合为一个确定性订单决策。
 */
public final class DecisionAggregator {

    private static final Map<Decision, Integer> PRIORITY = new EnumMap<>(Decision.class);

    static {
        PRIORITY.put(Decision.FREEZE, 700);
        PRIORITY.put(Decision.REJECT, 600);
        PRIORITY.put(Decision.COMPLIANCE_HOLD, 500);
        PRIORITY.put(Decision.OPS_REVIEW, 400);
        PRIORITY.put(Decision.REQUOTE, 300);
        PRIORITY.put(Decision.TEMPORARY_HOLD, 200);
        PRIORITY.put(Decision.COMPLETE, 100);
    }

    /**
     * 按决策优先级、原因码并集和升级团队并集聚合规则结果。
     *
     * @param results 单条规则结果列表
     * @return 聚合后的决策状态
     */
    public AggregatedDecision aggregate(List<RuleResult> results) {
        Decision topDecision = Decision.COMPLETE;
        Set<ReasonCode> reasons = EnumSet.noneOf(ReasonCode.class);
        Set<EscalationTarget> escalations = EnumSet.noneOf(EscalationTarget.class);
        Retryability retryability = Retryability.NOT_APPLICABLE;
        List<String> evidence = new java.util.ArrayList<>();

        for (RuleResult result : results) {
            reasons.addAll(result.reasonCodes());
            escalations.addAll(result.escalationTargets());
            evidence.addAll(result.evidence());
            retryability = stricter(retryability, result.retryability());
            if (result.blockingDecision().isPresent()) {
                topDecision = higherPriority(topDecision, result.blockingDecision().get());
            }
        }

        return new AggregatedDecision(topDecision, reasons, escalations, retryability, evidence);
    }

    private static Retryability stricter(Retryability left, Retryability right) {
        return retryPriority(right) > retryPriority(left) ? right : left;
    }

    private static int retryPriority(Retryability retryability) {
        return switch (retryability) {
            case NOT_APPLICABLE -> 1;
            case RETRYABLE -> 2;
            case NON_RETRYABLE -> 3;
        };
    }

    private static Decision higherPriority(Decision left, Decision right) {
        return PRIORITY.get(right) > PRIORITY.get(left) ? right : left;
    }

    /**
     * 聚合后的决策状态。
     *
     * @param decision 顶层处置结果
     * @param reasonCodes 全部原因码
     * @param escalationTargets 全部升级复核团队
     * @param retryability 最严格的自动重试语义
     * @param evidence 全部证据条目
     */
    public record AggregatedDecision(
            Decision decision,
            Set<ReasonCode> reasonCodes,
            Set<EscalationTarget> escalationTargets,
            Retryability retryability,
            List<String> evidence) {
    }
}
