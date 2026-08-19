package com.ramppolicy.engine.infrastructure.llm;

import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.ReasonCode;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * 默认离线解释器。
 */
public final class StubExplanationProvider implements ExplanationProvider {

    /**
     * 生成确定性的纯文本解释，不访问网络，也不依赖 API Key。
     *
     * @param request 解释请求
     * @return 离线解释结果
     */
    @Override
    public ExplanationResult explain(ExplanationRequest request) {
        String reasons = request.decision().reasonCodes().stream()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .map(Enum::name)
                .collect(Collectors.joining(","));
        String suffix = reasons.isBlank() ? "" : " ReasonCodes=[" + reasons + "]";
        return new ExplanationResult(messageFor(request.decision().decision()) + suffix, ExplanationProviderType.STUB, false, null);
    }

    private static String messageFor(Decision decision) {
        return switch (decision) {
            case COMPLETE -> "COMPLETE：所有强制规则和执行门都已通过。";
            case TEMPORARY_HOLD -> "TEMPORARY_HOLD：订单正在等待可重试的运维事实补齐。";
            case REQUOTE -> "REQUOTE：报价已过期或滑点超出允许范围。";
            case OPS_REVIEW -> "OPS_REVIEW：订单存在需要运维人工处理的数据或资金异常。";
            case COMPLIANCE_HOLD -> "COMPLIANCE_HOLD：订单需要合规复核后才能继续。";
            case REJECT -> "REJECT：订单在确定性策略下无法继续。";
            case FREEZE -> "FREEZE：订单命中冻结级风险，不能自动放行。";
        };
    }
}
