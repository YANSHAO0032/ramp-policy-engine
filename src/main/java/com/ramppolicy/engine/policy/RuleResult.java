package com.ramppolicy.engine.policy;

import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.EscalationTarget;
import com.ramppolicy.engine.domain.ReasonCode;
import com.ramppolicy.engine.domain.Retryability;
import com.ramppolicy.engine.plan.RuleId;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 单条计划规则产出的确定性结果。
 *
 * @param ruleId 产出该结果的规则标识
 * @param outcome 规则执行结果，分为通过、警告或阻断
 * @param blockingDecision 规则阻断时给出的处置结果
 * @param reasonCodes 结构化原因码集合
 * @param escalationTargets 升级复核团队集合
 * @param retryability 自动重试语义
 * @param evidence 规则证据文本
 */
public record RuleResult(
        RuleId ruleId,
        RuleOutcome outcome,
        Optional<Decision> blockingDecision,
        Set<ReasonCode> reasonCodes,
        Set<EscalationTarget> escalationTargets,
        Retryability retryability,
        List<String> evidence) {

    /**
     * 创建无原因和证据的通过结果。
     *
     * @param ruleId 规则标识
     * @return 通过结果
     */
    public static RuleResult pass(RuleId ruleId) {
        return new RuleResult(ruleId, RuleOutcome.PASS, Optional.empty(), Set.of(), Set.of(), Retryability.NOT_APPLICABLE, List.of());
    }

    /**
     * 创建提示性规则的警告结果。
     *
     * @param ruleId 规则标识
     * @param reasons 结构化原因码
     * @param escalations 升级复核团队
     * @param evidence 证据文本
     * @return 警告结果
     */
    public static RuleResult warn(RuleId ruleId, Set<ReasonCode> reasons, Set<EscalationTarget> escalations, List<String> evidence) {
        return new RuleResult(ruleId, RuleOutcome.WARN, Optional.empty(), reasons, escalations, Retryability.NOT_APPLICABLE, evidence);
    }

    /**
     * 创建会阻断资金动作的规则结果。
     *
     * @param ruleId 规则标识
     * @param decision 阻断处置结果
     * @param reasons 结构化原因码
     * @param escalations 升级复核团队
     * @param retryability 自动重试语义
     * @param evidence 证据文本
     * @return 阻断结果
     */
    public static RuleResult block(
            RuleId ruleId,
            Decision decision,
            Set<ReasonCode> reasons,
            Set<EscalationTarget> escalations,
            Retryability retryability,
            List<String> evidence) {
        return new RuleResult(ruleId, RuleOutcome.BLOCK, Optional.of(decision), reasons, escalations, retryability, evidence);
    }
}
