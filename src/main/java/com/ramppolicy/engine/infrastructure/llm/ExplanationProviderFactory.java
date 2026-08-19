package com.ramppolicy.engine.infrastructure.llm;

import java.util.Map;

/**
 * 根据配置创建解释器提供方。
 */
public final class ExplanationProviderFactory {

    private final LlmProperties properties;

    /**
     * 创建解释器提供方工厂。
     *
     * @param properties LLM 配置
     */
    public ExplanationProviderFactory(LlmProperties properties) {
        this.properties = properties;
    }

    /**
     * 创建当前配置对应的解释器提供方。
     *
     * @return 解释器提供方
     */
    public ExplanationProvider create() {
        StubExplanationProvider stub = new StubExplanationProvider();
        return switch (properties.provider()) {
            case STUB -> stub;
            case RECORDED -> new RecordedExplanationProvider(defaultRecordings(), stub);
            case OPENAI -> new SafeExplanationProvider(new OpenAiExplanationProvider(properties), stub, ExplanationProviderType.OPENAI);
        };
    }

    private static Map<String, String> defaultRecordings() {
        return Map.of(
                "FREEZE|ADDRESS_SANCTIONED", "录制回放：制裁地址需要冻结并升级合规复核。",
                "COMPLIANCE_HOLD|ADDRESS_HIGH_RISK", "录制回放：高风险对手方需要合规复核。");
    }
}
