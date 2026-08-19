package com.ramppolicy.engine.infrastructure.llm;

/**
 * 支持的解释器提供方模式。
 */
public enum ExplanationProviderType {
    /**
     * 默认离线解释器，不访问公网，不需要 API Key。
     */
    STUB,

    /**
     * 本地录制回放解释器，用于稳定演示和测试。
     */
    RECORDED,

    /**
     * 可选 OpenAI 解释器，仅在显式配置时启用。
     */
    OPENAI
}
