package com.ramppolicy.engine.runtime;

import java.util.List;

/**
 * Demo 订单批处理执行后的结果包。
 *
 * @param results 每单执行结果
 * @param auditLines 序列化后的审计行
 */
public record BatchRunResult(
        List<OrderExecutionRecord> results,
        List<String> auditLines) {
}
