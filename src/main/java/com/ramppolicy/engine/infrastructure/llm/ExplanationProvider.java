package com.ramppolicy.engine.infrastructure.llm;

/**
 * 项目自有的解释文本生成接口。
 */
@FunctionalInterface
public interface ExplanationProvider {

    /**
     * 为已经确定的策略决策生成纯文本解释。
     *
     * @param request 解释请求
     * @return 解释结果
     */
    ExplanationResult explain(ExplanationRequest request);
}
