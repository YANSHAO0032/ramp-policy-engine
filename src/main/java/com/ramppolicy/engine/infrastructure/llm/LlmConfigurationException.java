package com.ramppolicy.engine.infrastructure.llm;

/**
 * LLM 解释器配置错误的明确异常。
 */
public final class LlmConfigurationException extends RuntimeException {

    /**
     * 创建配置异常。
     *
     * @param message 可读的配置错误信息
     */
    public LlmConfigurationException(String message) {
        super(message);
    }
}
