package com.ramppolicy.engine.infrastructure.llm;

import com.ramppolicy.engine.domain.ReasonCode;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用于本地确定性回放的录制解释器。
 */
public final class RecordedExplanationProvider implements ExplanationProvider {

    private final Map<String, String> recordings;
    private final StubExplanationProvider fallback;

    /**
     * 使用内存录制文本创建录制回放解释器。
     *
     * @param recordings 回放键到解释文本的录制映射
     * @param fallback 确定性备用解释器
     */
    public RecordedExplanationProvider(Map<String, String> recordings, StubExplanationProvider fallback) {
        this.recordings = Map.copyOf(recordings);
        this.fallback = fallback;
    }

    /**
     * 按决策和排序后的原因码查找录制文本。
     *
     * @param request 解释请求
     * @return 录制文本，未命中时返回离线备用文本
     */
    @Override
    public ExplanationResult explain(ExplanationRequest request) {
        String text = recordings.get(key(request));
        if (text != null) {
            return new ExplanationResult(text, ExplanationProviderType.RECORDED, false, null);
        }
        ExplanationResult fallbackResult = fallback.explain(request);
        return new ExplanationResult(fallbackResult.text(), ExplanationProviderType.RECORDED, true, "RECORDED_MISS");
    }

    private static String key(ExplanationRequest request) {
        String reasons = request.decision().reasonCodes().stream()
                .sorted(Comparator.comparingInt(ReasonCode::ordinal))
                .map(Enum::name)
                .collect(Collectors.joining(","));
        return request.decision().decision().name() + "|" + reasons;
    }
}
