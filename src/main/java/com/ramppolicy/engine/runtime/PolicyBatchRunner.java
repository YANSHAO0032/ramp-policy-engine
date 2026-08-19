package com.ramppolicy.engine.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.DeterministicDecision;
import com.ramppolicy.engine.domain.OrderRecord;
import com.ramppolicy.engine.domain.OrderType;
import com.ramppolicy.engine.domain.ReasonCode;
import com.ramppolicy.engine.domain.Retryability;
import com.ramppolicy.engine.facts.DemoFacts;
import com.ramppolicy.engine.io.DemoDataLoader;
import com.ramppolicy.engine.io.JsonlOrderReader;
import com.ramppolicy.engine.policy.PolicyEngine;
import com.ramppolicy.engine.plan.StaticRulePlanResolver;
import com.ramppolicy.engine.idempotency.ClaimResult;
import com.ramppolicy.engine.idempotency.InMemoryOrderIdempotencyStore;
import com.ramppolicy.engine.idempotency.InMemoryTransactionIdempotencyStore;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Thin batch orchestrator for the vendored demo orders.
 */
public final class PolicyBatchRunner {

    private final DemoFacts facts;
    private final JsonlOrderReader reader;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final PolicyEngine policyEngine;
    private final InMemoryOrderIdempotencyStore orderStore = new InMemoryOrderIdempotencyStore();
    private final InMemoryTransactionIdempotencyStore transactionStore = new InMemoryTransactionIdempotencyStore();
    private final ActionExecutor actionExecutor = new ActionExecutor();

    /**
     * Creates a batch runner for the demo dataset.
     *
     * @param facts authoritative demo facts
     * @param reader JSONL order reader
     * @param objectMapper JSON serializer
     * @param clock evaluation clock
     */
    public PolicyBatchRunner(DemoFacts facts, JsonlOrderReader reader, ObjectMapper objectMapper, Clock clock) {
        this.facts = facts;
        this.reader = reader;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.policyEngine = new PolicyEngine(new StaticRulePlanResolver(), facts, clock);
    }

    /**
     * Runs the vendored demo orders from the classpath and writes outputs.
     *
     * @param outputDir output directory
     * @return batch result bundle
     * @throws IOException when input or output fails
     */
    public BatchRunResult runDemo(Path outputDir) throws IOException {
        DemoDataLoader loader = new DemoDataLoader(objectMapper);
        List<OrderRecord> orders = loader.loadOrdersFromClasspath("demo-data/orders.jsonl");
        return runOrders(orders, outputDir);
    }

    /**
     * Runs a batch from a filesystem JSONL file and writes outputs.
     *
     * @param ordersPath JSONL file path
     * @param outputDir output directory
     * @return batch result bundle
     * @throws IOException when input or output fails
     */
    public BatchRunResult run(Path ordersPath, Path outputDir) throws IOException {
        List<OrderRecord> orders = reader.readAll(Files.readString(ordersPath, StandardCharsets.UTF_8));
        return runOrders(orders, outputDir);
    }

    private BatchRunResult runOrders(List<OrderRecord> orders, Path outputDir) throws IOException {
        List<OrderExecutionRecord> results = new ArrayList<>();
        List<String> auditLines = new ArrayList<>();

        for (int i = 0; i < orders.size(); i++) {
            results.add(executeOne(orders.get(i), "intake-" + i, auditLines));
        }

        Files.createDirectories(outputDir);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputDir.resolve("results.json").toFile(), results);
        Files.write(outputDir.resolve("audit.jsonl"), auditLines, StandardCharsets.UTF_8);
        return new BatchRunResult(List.copyOf(results), List.copyOf(auditLines));
    }

    private OrderExecutionRecord executeOne(OrderRecord order, String intakeOwner, List<String> auditLines) throws IOException {
        ClaimResult orderClaim = orderStore.claim(order.orderId(), intakeOwner);
        if (!orderClaim.accepted()) {
            DeterministicDecision decision = duplicateDecision(order.orderId(), ReasonCode.DUPLICATE_ORDER);
            return record(order, decision, false, null, auditLines);
        }

        DeterministicDecision decision = policyEngine.evaluate(order);
        boolean transactionClaimAccepted = true;
        if (decision.decision() == Decision.COMPLETE && order.type() == OrderType.OFF_RAMP && order.deposit() != null) {
            String txHash = order.deposit().txHash();
            ClaimResult txClaim = transactionStore.claim(order.asset(), order.network(), txHash, order.orderId());
            transactionClaimAccepted = txClaim.accepted();
            if (!transactionClaimAccepted) {
                decision = duplicateTransactionDecision(order, decision);
            }
        }

        ActionExecutor.ActionResult action = actionExecutor.execute(order, decision.decision(), orderClaim.accepted(), transactionClaimAccepted);
        return record(order, decision, action.executed(), action.actionType(), auditLines);
    }

    private OrderExecutionRecord record(OrderRecord order, DeterministicDecision decision, boolean actionExecuted, String actionType, List<String> auditLines) throws IOException {
        OrderExecutionRecord record = new OrderExecutionRecord(
                order.orderId(),
                decision.decision(),
                decision.reasonCodes(),
                decision.escalationTargets(),
                decision.retryability(),
                actionExecuted,
                actionType,
                decision.evidence());
        auditLines.add(objectMapper.writeValueAsString(new AuditEntry(order.orderId(), decision.decision().name(), decision.reasonCodes().stream().map(Enum::name).toList(), actionExecuted)));
        return record;
    }

    private DeterministicDecision duplicateDecision(String orderId, ReasonCode reasonCode) {
        return new DeterministicDecision(
                orderId,
                Decision.OPS_REVIEW,
                java.util.Set.of(reasonCode),
                java.util.Set.of(),
                Retryability.NON_RETRYABLE,
                List.of("idempotency gate blocked"),
                com.ramppolicy.engine.domain.PolicyVersion.VALUE,
                clock.instant());
    }

    private DeterministicDecision duplicateTransactionDecision(OrderRecord order, DeterministicDecision base) {
        EnumSet<ReasonCode> reasons = base.reasonCodes().isEmpty()
                ? EnumSet.noneOf(ReasonCode.class)
                : EnumSet.copyOf(base.reasonCodes());
        reasons.add(ReasonCode.DUPLICATE_TRANSACTION);
        return new DeterministicDecision(
                order.orderId(),
                Decision.OPS_REVIEW,
                EnumSet.copyOf(reasons),
                base.escalationTargets(),
                base.retryability(),
                base.evidence(),
                base.policyVersion(),
                base.evaluatedAt());
    }

    /**
     * Minimal audit row emitted to the JSONL trail.
     *
     * @param orderId order identifier
     * @param decision decision name
     * @param reasons reason names
     * @param actionExecuted whether a funds action executed
     */
    public record AuditEntry(String orderId, String decision, List<String> reasons, boolean actionExecuted) {
    }
}
