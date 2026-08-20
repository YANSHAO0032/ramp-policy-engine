package com.ramppolicy.engine.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.DeterministicDecision;
import com.ramppolicy.engine.domain.OrderRecord;
import com.ramppolicy.engine.domain.OrderType;
import com.ramppolicy.engine.domain.ReasonCode;
import com.ramppolicy.engine.domain.Retryability;
import com.ramppolicy.engine.facts.DemoFacts;
import com.ramppolicy.engine.infrastructure.llm.ExplanationProvider;
import com.ramppolicy.engine.infrastructure.llm.ExplanationProviderFactory;
import com.ramppolicy.engine.infrastructure.llm.ExplanationRequest;
import com.ramppolicy.engine.infrastructure.llm.ExplanationResult;
import com.ramppolicy.engine.infrastructure.llm.LlmProperties;
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
 * 仓库内置 Demo 订单的轻量批处理编排器。
 */
public final class PolicyBatchRunner {

    private final DemoFacts facts;
    private final JsonlOrderReader reader;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final PolicyEngine policyEngine;
    private final ExplanationProvider explanationProvider;
    private final InMemoryOrderIdempotencyStore orderStore = new InMemoryOrderIdempotencyStore();
    private final InMemoryTransactionIdempotencyStore transactionStore = new InMemoryTransactionIdempotencyStore();
    private final ActionExecutor actionExecutor = new ActionExecutor();

    /**
     * 使用默认环境变量配置的解释器创建 Demo 批处理运行器。
     *
     * @param facts Demo 权威事实集合
     * @param reader JSONL 订单解析器
     * @param objectMapper JSON 序列化器
     * @param clock 评估时钟
     */
    public PolicyBatchRunner(DemoFacts facts, JsonlOrderReader reader, ObjectMapper objectMapper, Clock clock) {
        this(facts, reader, objectMapper, clock, new ExplanationProviderFactory(LlmProperties.from(System.getenv())).create());
    }

    /**
     * 使用显式解释器创建 Demo 批处理运行器。
     *
     * @param facts Demo 权威事实集合
     * @param reader JSONL 订单解析器
     * @param objectMapper JSON 序列化器
     * @param clock 评估时钟
     * @param explanationProvider 解释器，仅用于生成非权威说明
     */
    public PolicyBatchRunner(DemoFacts facts, JsonlOrderReader reader, ObjectMapper objectMapper, Clock clock, ExplanationProvider explanationProvider) {
        this.facts = facts;
        this.reader = reader;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.policyEngine = new PolicyEngine(new StaticRulePlanResolver(), facts, clock);
        this.explanationProvider = explanationProvider;
    }

    /**
     * 从 classpath 读取内置 Demo 订单并写出结果文件。
     *
     * @param outputDir 输出目录
     * @return 批处理结果
     * @throws IOException 输入或输出失败时抛出
     */
    public BatchRunResult runDemo(Path outputDir) throws IOException {
        DemoDataLoader loader = new DemoDataLoader(objectMapper);
        List<OrderRecord> orders = loader.loadOrdersFromClasspath("demo-data/orders.jsonl");
        return runOrders(orders, outputDir);
    }

    /**
     * 从文件系统 JSONL 文件运行批处理并写出结果文件。
     *
     * @param ordersPath JSONL 订单文件路径
     * @param outputDir 输出目录
     * @return 批处理结果
     * @throws IOException 输入或输出失败时抛出
     */
    public BatchRunResult run(Path ordersPath, Path outputDir) throws IOException {
        List<OrderRecord> orders = reader.readAll(Files.readString(ordersPath, StandardCharsets.UTF_8));
        return runOrders(orders, outputDir);
    }

    /**
     * 逐单执行批处理，并落盘结果与审计轨迹。
     *
     * @param orders 待处理订单列表
     * @param outputDir 输出目录
     * @return 批处理结果
     * @throws IOException 写文件失败时抛出
     */
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

    /**
     * 执行单笔订单的幂等占用、策略评估、动作执行与审计记录。
     *
     * @param order 当前订单
     * @param intakeOwner 订单归属的 intake 标识
     * @param auditLines 审计行收集器
     * @return 订单执行记录
     * @throws IOException 生成审计行失败时抛出
     */
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

    /**
     * 将决策、解释和审计信息组装为最终执行记录。
     *
     * @param order 当前订单
     * @param decision 已确定的策略决策
     * @param actionExecuted 是否实际执行资金动作
     * @param actionType 动作类型
     * @param auditLines 审计行收集器
     * @return 订单执行记录
     * @throws IOException 序列化审计行失败时抛出
     */
    private OrderExecutionRecord record(OrderRecord order, DeterministicDecision decision, boolean actionExecuted, String actionType, List<String> auditLines) throws IOException {
        ExplanationResult explanation = explanationProvider.explain(new ExplanationRequest(order.orderId(), decision));
        OrderExecutionRecord record = new OrderExecutionRecord(
                order.orderId(),
                decision.decision(),
                decision.reasonCodes(),
                decision.escalationTargets(),
                decision.retryability(),
                actionExecuted,
                actionType,
                decision.evidence(),
                explanation.text(),
                explanation.provider(),
                explanation.fallbackUsed());
        auditLines.add(objectMapper.writeValueAsString(new AuditEntry(
                order.orderId(),
                decision.decision().name(),
                decision.reasonCodes().stream().map(Enum::name).toList(),
                actionExecuted,
                explanation.provider().name(),
                explanation.fallbackUsed(),
                explanation.failureCode())));
        return record;
    }

    /**
     * 为重复订单构造固定的拒绝/复核决策。
     *
     * @param orderId 订单标识
     * @param reasonCode 触发重复的原因码
     * @return 重复订单决策
     */
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

    /**
     * 在原始决策基础上补充重复交易原因。
     *
     * @param order 当前订单
     * @param base 原始决策
     * @return 补充重复交易后的决策
     */
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
     * 写入 JSONL 审计轨迹的最小审计行。
     *
     * @param orderId 订单标识
     * @param decision 决策名称
     * @param reasons 原因码名称列表
     * @param actionExecuted 是否执行资金动作
     * @param explanationProvider 解释器类型
     * @param explanationFallbackUsed 是否使用 fallback 解释
     * @param explanationFailureCode 解释器失败码，未失败时为空
     */
    public record AuditEntry(
            String orderId,
            String decision,
            List<String> reasons,
            boolean actionExecuted,
            String explanationProvider,
            boolean explanationFallbackUsed,
            String explanationFailureCode) {
    }
}
