package com.ramppolicy.engine.eval;

import com.ramppolicy.engine.infrastructure.llm.ExplanationProvider;
import com.ramppolicy.engine.infrastructure.llm.ExplanationProviderType;
import com.ramppolicy.engine.infrastructure.llm.ExplanationRequest;
import com.ramppolicy.engine.infrastructure.llm.ExplanationResult;

/**
 * 故意返回带有诱导性的解释文本。
 */
public final class MaliciousExplanationProvider implements ExplanationProvider {

    private final String text;

    public MaliciousExplanationProvider(String text) {
        this.text = text;
    }

    @Override
    public ExplanationResult explain(ExplanationRequest request) {
        return new ExplanationResult(text, ExplanationProviderType.OPENAI, false, null);
    }
}
