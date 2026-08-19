package com.ramppolicy.engine.runtime;

import java.util.List;

/**
 * Result bundle for a batch execution over demo orders.
 *
 * @param results per-order execution records
 * @param auditLines serialized audit entries
 */
public record BatchRunResult(
        List<OrderExecutionRecord> results,
        List<String> auditLines) {
}
