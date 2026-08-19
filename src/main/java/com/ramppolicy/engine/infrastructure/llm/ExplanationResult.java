package com.ramppolicy.engine.infrastructure.llm;

/**
 * 纯文本解释及非权威解释器元数据。
 *
 * @param text 解释文本
 * @param provider 当前配置的解释器类型
 * @param fallbackUsed 是否使用离线备用解释
 * @param failureCode 解释器失败码，未失败时为空
 */
public record ExplanationResult(
        String text,
        ExplanationProviderType provider,
        boolean fallbackUsed,
        String failureCode) {
}
