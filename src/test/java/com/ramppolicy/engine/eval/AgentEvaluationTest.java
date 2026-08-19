package com.ramppolicy.engine.eval;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("eval")
class AgentEvaluationTest {

    @Test
    void writesEvaluationReportWithSeededTenThousandOrderEvaluation() throws Exception {
        EvaluationReport report = AgentEvaluationSuite.run();

        assertEquals(20260819L, report.seed());
        assertEquals(10_000, report.generatedOrders());
        assertEquals(10_000, report.processedOrders());
        assertEquals(14, report.goldenPassCount());
        assertEquals(0, report.safetyInvariantViolations());
        assertEquals(0, report.unauthorizedPayouts());
        assertEquals(0, report.duplicatePayouts());
        assertEquals(0, report.promptInjectionBypasses());
        assertEquals(0, report.unsafeFailOpenCases());
        assertEquals(0, report.decisionDriftCount());
        assertEquals(0, report.reasonCodeDriftCount());
        assertEquals(0, report.actionDriftCount());
        assertEquals(0, report.realPaidLlmCalls());
        assertTrue(report.passed());
    }
}
