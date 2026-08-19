package com.ramppolicy.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ramppolicy.engine.facts.DemoFacts;
import com.ramppolicy.engine.infrastructure.llm.ExplanationProvider;
import com.ramppolicy.engine.infrastructure.llm.ExplanationProviderFactory;
import com.ramppolicy.engine.infrastructure.llm.LlmProperties;
import com.ramppolicy.engine.io.JsonlOrderReader;
import com.ramppolicy.engine.runtime.PolicyBatchRunner;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * 出入金策略引擎 Demo 应用入口。
 */
public final class App {

    private App() {
    }

    /**
     * 端到端运行 Demo 批处理流水线。
     *
     * @param args 当前未使用的命令行参数
     * @throws Exception 批处理执行失败时抛出
     */
    public static void main(String[] args) throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-07-28T12:00:00Z"), ZoneOffset.UTC);
        ObjectMapper objectMapper = new ObjectMapper();
        DemoFacts facts = DemoFacts.loadFromClasspath(Path.of("demo-data"), objectMapper);
        ExplanationProvider explanationProvider = new ExplanationProviderFactory(LlmProperties.from(System.getenv())).create();
        PolicyBatchRunner runner = new PolicyBatchRunner(facts, new JsonlOrderReader(objectMapper), objectMapper, clock, explanationProvider);
        runner.runDemo(Path.of("output"));
        System.out.println("ramp-policy-engine batch complete");
    }
}
