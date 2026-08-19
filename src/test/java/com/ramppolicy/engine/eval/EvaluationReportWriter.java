package com.ramppolicy.engine.eval;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 评测报告落盘器。
 */
public final class EvaluationReportWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private EvaluationReportWriter() {
    }

    public static void write(EvaluationReport report, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(outputDir.resolve("evaluation-report.json").toFile(), report);
        Files.writeString(outputDir.resolve("evaluation-report.md"), markdown(report), StandardCharsets.UTF_8);
    }

    public static String markdown(EvaluationReport report) {
        return """
                # Agent Evaluation Report

                Commit: %s
                Seed: %d
                Generated orders: %d
                Processed orders: %d

                | Metric | Result |
                |---|---:|
                | Golden regression | %d / %d |
                | Boundary cases | %d / %d |
                | Conflict cases | %d / %d |
                | Safety invariant checks | %d |
                | Safety invariant violations | %d |
                | Unauthorized payouts | %d |
                | Duplicate payouts | %d |
                | Prompt injection cases | %d |
                | Prompt injection bypasses | %d |
                | Tool failure cases | %d |
                | Unsafe fail-open cases | %d |
                | LLM variation calls | %d |
                | Decision drift | %d |
                | ReasonCode drift | %d |
                | Action drift | %d |
                | Paid LLM calls | %d |
                | Total duration ms | %d |
                | Final result | %s |
                """.formatted(
                report.commitSha(),
                report.seed(),
                report.generatedOrders(),
                report.processedOrders(),
                report.goldenPassCount(),
                report.goldenCases(),
                report.boundaryPassCount(),
                report.boundaryCases(),
                report.conflictPassCount(),
                report.conflictCases(),
                report.safetyInvariantChecks(),
                report.safetyInvariantViolations(),
                report.unauthorizedPayouts(),
                report.duplicatePayouts(),
                report.promptInjectionCases(),
                report.promptInjectionBypasses(),
                report.toolFailureCases(),
                report.unsafeFailOpenCases(),
                report.llmVariationCalls(),
                report.decisionDriftCount(),
                report.reasonCodeDriftCount(),
                report.actionDriftCount(),
                report.realPaidLlmCalls(),
                report.totalDurationMs(),
                report.passed() ? "PASS" : "FAIL");
    }
}
