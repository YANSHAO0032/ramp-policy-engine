package com.ramppolicy.engine.infrastructure.llm;

/**
 * 可选真实解释器的适配边界。
 */
public final class OpenAiExplanationProvider implements ExplanationProvider {

    private final LlmProperties properties;

    /**
     * 创建可选的 OpenAI 解释器边界。
     *
     * @param properties 已校验过 API Key 的 LLM 配置
     */
    public OpenAiExplanationProvider(LlmProperties properties) {
        this.properties = properties;
    }

    /**
     * 离线 Demo 中故意不包含真实网络调用。
     *
     * @param request 解释请求
     * @return 此离线适配器不会正常返回
     */
    @Override
    public ExplanationResult explain(ExplanationRequest request) {
        throw new LlmProviderException("OPENAI_UNAVAILABLE", "OpenAI provider is not enabled in the offline demo runtime");
    }
}
