package com.ramppolicy.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ramppolicy.engine.domain.UtcClock;
import com.ramppolicy.engine.facts.DemoFacts;
import com.ramppolicy.engine.io.JsonlOrderReader;
import com.ramppolicy.engine.runtime.PolicyBatchRunner;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Demo application entry point for the ramp policy engine.
 */
public final class App {

    private App() {
    }

    /**
     * Runs the demo batch pipeline end to end.
     *
     * @param args ignored CLI arguments
     * @throws Exception when batch execution fails
     */
    public static void main(String[] args) throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-07-28T12:00:00Z"), ZoneOffset.UTC);
        DemoFacts facts = DemoFacts.loadFromClasspath(Path.of("demo-data"), new ObjectMapper());
        PolicyBatchRunner runner = new PolicyBatchRunner(facts, new JsonlOrderReader(new ObjectMapper()), new ObjectMapper(), clock);
        runner.runDemo(Path.of("output"));
        System.out.println("ramp-policy-engine batch complete");
    }
}
