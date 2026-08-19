package com.ramppolicy.engine.eval;

import com.ramppolicy.engine.infrastructure.llm.ExplanationProvider;
import com.ramppolicy.engine.infrastructure.llm.ExplanationRequest;
import com.ramppolicy.engine.infrastructure.llm.ExplanationResult;
import com.ramppolicy.engine.infrastructure.llm.LlmProviderException;

/**
 * 始终失败的解释器模拟器。
 */
public final class FailingExplanationProvider implements ExplanationProvider {

    private final String failureCode;

    public FailingExplanationProvider(String failureCode) {
        this.failureCode = failureCode;
    }

    @Override
    public ExplanationResult explain(ExplanationRequest request) {
        throw new LlmProviderException(failureCode, "synthetic provider failure");
    }
}
