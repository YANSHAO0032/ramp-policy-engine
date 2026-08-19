package com.ramppolicy.engine.infrastructure.llm;

/**
 * 运行时解释器故障，调用方应回退到离线解释。
 */
public final class LlmProviderException extends RuntimeException {

    private final String failureCode;

    /**
     * 创建解释器运行时异常。
     *
     * @param failureCode 稳定失败码
     * @param message 诊断信息
     */
    public LlmProviderException(String failureCode, String message) {
        super(message);
        this.failureCode = failureCode;
    }

    /**
     * 返回可写入审计元数据的稳定失败码。
     *
     * @return 失败码
     */
    public String failureCode() {
        return failureCode;
    }
}
