package com.ramppolicy.engine.infrastructure.llm;

/**
 * 在运行时解释器故障时回退到确定性离线解释的包装器。
 */
public final class SafeExplanationProvider implements ExplanationProvider {

    private final ExplanationProvider delegate;
    private final StubExplanationProvider fallback;
    private final ExplanationProviderType configuredProvider;

    /**
     * 创建安全包装器。
     *
     * @param delegate 已配置的解释器
     * @param fallback 确定性备用解释器
     * @param configuredProvider 已配置解释器的元数据
     */
    public SafeExplanationProvider(
            ExplanationProvider delegate,
            StubExplanationProvider fallback,
            ExplanationProviderType configuredProvider) {
        this.delegate = delegate;
        this.fallback = fallback;
        this.configuredProvider = configuredProvider;
    }

    /**
     * 发生运行时解释器故障时自动回退到离线解释。
     *
     * @param request 解释请求
     * @return 解释结果
     */
    @Override
    public ExplanationResult explain(ExplanationRequest request) {
        try {
            return delegate.explain(request);
        } catch (RuntimeException ex) {
            ExplanationResult fallbackResult = fallback.explain(request);
            String code = ex instanceof LlmProviderException providerException
                    ? providerException.failureCode()
                    : "PROVIDER_FAILURE";
            return new ExplanationResult(fallbackResult.text(), configuredProvider, true, code);
        }
    }
}
