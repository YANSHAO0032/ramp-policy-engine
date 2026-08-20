package com.ramppolicy.engine.policy;

import com.ramppolicy.engine.domain.AddressRiskRecord;
import com.ramppolicy.engine.domain.AssetNetworkRecord;
import com.ramppolicy.engine.domain.CustomerRecord;
import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.DeterministicDecision;
import com.ramppolicy.engine.domain.EscalationTarget;
import com.ramppolicy.engine.domain.OrderRecord;
import com.ramppolicy.engine.domain.OrderType;
import com.ramppolicy.engine.domain.PolicyVersion;
import com.ramppolicy.engine.domain.ReasonCode;
import com.ramppolicy.engine.domain.Retryability;
import com.ramppolicy.engine.domain.UtcClock;
import com.ramppolicy.engine.facts.DemoFacts;
import com.ramppolicy.engine.facts.UsdValuationService;
import com.ramppolicy.engine.plan.PlannedRule;
import com.ramppolicy.engine.plan.RuleId;
import com.ramppolicy.engine.plan.RulePlan;
import com.ramppolicy.engine.plan.RulePlanResolver;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 面向 Demo 数据集的确定性策略引擎。
 */
public final class PolicyEngine {

    private static final Instant GOLDEN_NOW = Instant.parse("2026-07-28T12:00:00Z");
    private static final BigDecimal TRAVEL_RULE_THRESHOLD_USD = new BigDecimal("1000");

    private final RulePlanResolver resolver;
    private final DemoFacts facts;
    private final UsdValuationService valuationService;
    private final Clock clock;
    private final DecisionAggregator aggregator = new DecisionAggregator();

    /**
     * 使用显式时钟创建策略引擎，便于测试和可复现评估。
     *
     * @param resolver 规则计划解析器
     * @param facts Demo 权威事实集合
     * @param clock 评估时钟
     */
    public PolicyEngine(RulePlanResolver resolver, DemoFacts facts, Clock clock) {
        this.resolver = resolver;
        this.facts = facts;
        this.valuationService = new UsdValuationService(facts.referenceRates());
        this.clock = clock;
    }

    /**
     * 使用固定 Demo 时钟创建策略引擎。
     *
     * @param resolver 规则计划解析器
     * @param facts Demo 权威事实集合
     */
    public PolicyEngine(RulePlanResolver resolver, DemoFacts facts) {
        this(resolver, facts, UtcClock.fixed(GOLDEN_NOW));
    }

    /**
     * 按订单类型解析规则计划并评估单个订单。
     *
     * @param order 已解析订单
     * @return 确定性决策
     */
    public DeterministicDecision evaluate(OrderRecord order) {
        RulePlan plan = resolver.resolve(order.type());
        CustomerRecord customer = facts.customers().get(order.customerId());
        AssetNetworkRecord asset = facts.asset(order.asset(), order.network());
        List<RuleResult> results = new ArrayList<>();

        for (PlannedRule plannedRule : plan.rules()) {
            RuleResult result = evaluatePlannedRule(plannedRule.ruleId(), order, customer, asset);
            if (result != null) {
                results.add(result);
            }
        }

        DecisionAggregator.AggregatedDecision aggregated = aggregator.aggregate(results);
        return new DeterministicDecision(
                order.orderId(),
                aggregated.decision(),
                aggregated.reasonCodes(),
                aggregated.escalationTargets(),
                aggregated.retryability(),
                aggregated.evidence(),
                PolicyVersion.VALUE,
                clock.instant());
    }

    /**
     * 按规则标识调用对应的规则检查逻辑。
     *
     * @param ruleId 规则标识
     * @param order 当前订单
     * @param customer 订单关联客户
     * @param asset 订单关联资产与网络配置
     * @return 单条规则的结果
     */
    private RuleResult evaluatePlannedRule(
            RuleId ruleId,
            OrderRecord order,
            CustomerRecord customer,
            AssetNetworkRecord asset) {
        return switch (ruleId) {
            case CUSTOMER_STATUS -> evaluateCustomerStatus(customer);
            case ASSET_SUPPORT -> evaluateAssetSupport(order, asset);
            case ADDRESS_RISK -> evaluateAddressRisk(order);
            case KYC_LIMIT -> evaluateKycLimit(order, customer, asset);
            case MINIMUM_AMOUNT -> evaluateMinimumAmount(order, asset);
            case FIAT_RECEIPT -> evaluateFiatReceipt(order);
            case ON_RAMP_CONSERVATION -> evaluateOnRampConservation(order, asset);
            case CONFIRMATION -> evaluateConfirmation(order, asset);
            case AMOUNT_MATCH -> evaluateAmountMatch(order);
            case PAYOUT_CONSERVATION -> evaluatePayoutConservation(order, asset);
            case NETWORK_MATCH -> evaluateNetworkMatch(order);
            case BANK_OWNERSHIP -> evaluateBankOwnership(order, customer);
            case QUOTE_EXPIRY -> evaluateQuoteExpiry(order, asset);
            case TRAVEL_RULE -> evaluateTravelRule(order, asset);
            case WITHDRAWAL_FUNDS -> evaluateWithdrawalFunds(order);
            case VASP_UNKNOWN_WARNING -> evaluateVaspUnknownWarning(order, asset);
        };
    }

    /**
     * 校验客户状态是否仍然可处理。
     *
     * @param customer 客户事实
     * @return 客户状态规则结果
     */
    private RuleResult evaluateCustomerStatus(CustomerRecord customer) {
        if (customer == null) {
            return RuleResult.block(RuleId.CUSTOMER_STATUS, Decision.OPS_REVIEW,
                    Set.of(ReasonCode.CUSTOMER_NOT_FOUND), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE, List.of("customer missing"));
        }
        if (!"active".equals(customer.status())) {
            return RuleResult.block(RuleId.CUSTOMER_STATUS, Decision.COMPLIANCE_HOLD,
                    Set.of(ReasonCode.CUSTOMER_NOT_ACTIVE), Set.of(EscalationTarget.COMPLIANCE), Retryability.NON_RETRYABLE, List.of("customer status=" + customer.status()));
        }
        return RuleResult.pass(RuleId.CUSTOMER_STATUS);
    }

    /**
     * 校验订单声明的资产和网络是否受支持。
     *
     * @param order 当前订单
     * @param asset 资产网络配置
     * @return 资产支持规则结果
     */
    private RuleResult evaluateAssetSupport(OrderRecord order, AssetNetworkRecord asset) {
        if (asset == null) {
            return RuleResult.block(RuleId.ASSET_SUPPORT, Decision.OPS_REVIEW,
                    Set.of(ReasonCode.UNSUPPORTED_ASSET), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE,
                    List.of("asset=" + order.asset(), "network=" + order.network()));
        }
        return RuleResult.pass(RuleId.ASSET_SUPPORT);
    }

    /**
     * 校验订单目标地址或来源地址的风险等级。
     *
     * @param order 当前订单
     * @return 地址风险规则结果
     */
    private RuleResult evaluateAddressRisk(OrderRecord order) {
        String address = switch (order.type()) {
            case ON_RAMP, WITHDRAWAL -> order.destinationAddress();
            case OFF_RAMP -> order.deposit() == null ? null : order.deposit().fromAddress();
        };
        if (address == null || address.isBlank()) {
            return RuleResult.block(RuleId.ADDRESS_RISK, Decision.OPS_REVIEW,
                    Set.of(order.type() == OrderType.OFF_RAMP ? ReasonCode.SOURCE_ADDRESS_MISSING : ReasonCode.DESTINATION_ADDRESS_MISSING),
                    Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE, List.of("address missing"));
        }
        AddressRiskRecord risk = facts.addressRisks().get(address);
        if (risk == null) {
            return RuleResult.block(RuleId.ADDRESS_RISK, Decision.COMPLIANCE_HOLD,
                    Set.of(ReasonCode.ADDRESS_UNKNOWN), Set.of(EscalationTarget.COMPLIANCE), Retryability.NON_RETRYABLE, List.of("address=" + address));
        }
        String category = risk.category();
        if ("sanctioned".equals(category)) {
            return RuleResult.block(RuleId.ADDRESS_RISK, Decision.FREEZE,
                    Set.of(ReasonCode.ADDRESS_SANCTIONED), Set.of(EscalationTarget.COMPLIANCE), Retryability.NON_RETRYABLE, List.of("address=" + address));
        }
        if ("mixer".equals(category) || "darknet".equals(category)) {
            return RuleResult.block(RuleId.ADDRESS_RISK, Decision.COMPLIANCE_HOLD,
                    Set.of(ReasonCode.ADDRESS_HIGH_RISK), Set.of(EscalationTarget.COMPLIANCE), Retryability.NON_RETRYABLE, List.of("address=" + address, "category=" + category));
        }
        if (risk.riskScore() >= 90) {
            return RuleResult.block(RuleId.ADDRESS_RISK, Decision.FREEZE,
                    Set.of(ReasonCode.ADDRESS_HIGH_RISK), Set.of(EscalationTarget.COMPLIANCE), Retryability.NON_RETRYABLE, List.of("score=" + risk.riskScore()));
        }
        if (risk.riskScore() >= 70) {
            return RuleResult.block(RuleId.ADDRESS_RISK, Decision.COMPLIANCE_HOLD,
                    Set.of(ReasonCode.ADDRESS_HIGH_RISK), Set.of(EscalationTarget.COMPLIANCE), Retryability.NON_RETRYABLE, List.of("score=" + risk.riskScore()));
        }
        return RuleResult.pass(RuleId.ADDRESS_RISK);
    }

    /**
     * 校验单笔订单对应的 KYC 限额。
     *
     * @param order 当前订单
     * @param customer 订单关联客户
     * @param asset 资产网络配置
     * @return KYC 限额规则结果
     */
    private RuleResult evaluateKycLimit(OrderRecord order, CustomerRecord customer, AssetNetworkRecord asset) {
        if (customer == null || asset == null) {
            return RuleResult.pass(RuleId.KYC_LIMIT);
        }
        BigDecimal valueUsd = switch (order.type()) {
            case ON_RAMP -> order.fiatAmountUsd();
            case OFF_RAMP -> valuationService.cryptoToUsd(order.asset(), max(order.quotedCryptoAmount(),
                    order.deposit() == null ? null : order.deposit().observedAmount()));
            case WITHDRAWAL -> valuationService.cryptoToUsd(order.asset(), order.amount());
        };
        if (valueUsd != null && valueUsd.compareTo(customer.monthlyLimitUsd()) > 0) {
            return RuleResult.block(RuleId.KYC_LIMIT, Decision.COMPLIANCE_HOLD,
                    Set.of(ReasonCode.KYC_LIMIT_EXCEEDED), Set.of(EscalationTarget.COMPLIANCE), Retryability.NON_RETRYABLE, List.of("valueUsd=" + valueUsd));
        }
        return RuleResult.pass(RuleId.KYC_LIMIT);
    }

    /**
     * 校验订单金额是否达到资产的最小要求。
     *
     * @param order 当前订单
     * @param asset 资产网络配置
     * @return 最小金额规则结果
     */
    private RuleResult evaluateMinimumAmount(OrderRecord order, AssetNetworkRecord asset) {
        if (asset == null) {
            return RuleResult.pass(RuleId.MINIMUM_AMOUNT);
        }
        BigDecimal amount = switch (order.type()) {
            case ON_RAMP -> order.quotedCryptoAmount();
            case OFF_RAMP -> order.deposit() == null ? null : order.deposit().observedAmount();
            case WITHDRAWAL -> order.amount();
        };
        if (amount == null) {
            return RuleResult.pass(RuleId.MINIMUM_AMOUNT);
        }
        if (amount.compareTo(asset.minAmount()) < 0) {
            return RuleResult.block(RuleId.MINIMUM_AMOUNT, Decision.OPS_REVIEW,
                    Set.of(ReasonCode.BELOW_MIN_AMOUNT), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE,
                    List.of("amount=" + amount, "min=" + asset.minAmount()));
        }
        return RuleResult.pass(RuleId.MINIMUM_AMOUNT);
    }

    /**
     * 校验法币入账状态。
     *
     * @param order 当前订单
     * @return 法币入账规则结果，非 on-ramp 时返回 null
     */
    private RuleResult evaluateFiatReceipt(OrderRecord order) {
        if (order.type() != OrderType.ON_RAMP) {
            return null;
        }
        String status = order.fiatStatus();
        if (status == null) {
            return RuleResult.block(RuleId.FIAT_RECEIPT, Decision.OPS_REVIEW,
                    Set.of(ReasonCode.FIAT_STATUS_MISSING), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE, List.of("fiat status missing"));
        }
        return switch (status) {
            case "received" -> RuleResult.pass(RuleId.FIAT_RECEIPT);
            case "pending" -> RuleResult.block(RuleId.FIAT_RECEIPT, Decision.TEMPORARY_HOLD,
                    Set.of(ReasonCode.FIAT_NOT_RECEIVED), Set.of(EscalationTarget.OPS), Retryability.RETRYABLE, List.of("fiat pending"));
            case "failed" -> RuleResult.block(RuleId.FIAT_RECEIPT, Decision.REJECT,
                    Set.of(ReasonCode.FIAT_PAYMENT_FAILED), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE, List.of("fiat failed"));
            case "reversed" -> RuleResult.block(RuleId.FIAT_RECEIPT, Decision.REJECT,
                    Set.of(ReasonCode.FIAT_PAYMENT_REVERSED), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE, List.of("fiat reversed"));
            default -> RuleResult.block(RuleId.FIAT_RECEIPT, Decision.OPS_REVIEW,
                    Set.of(ReasonCode.INVALID_ORDER_DATA), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE, List.of("fiat status=" + status));
        };
    }

    /**
     * 校验法币入金与预计出币的金额守恒。
     *
     * @param order 当前订单
     * @param asset 资产网络配置
     * @return 入金守恒规则结果
     */
    private RuleResult evaluateOnRampConservation(OrderRecord order, AssetNetworkRecord asset) {
        if (order.type() != OrderType.ON_RAMP || asset == null) {
            return null;
        }
        BigDecimal confirmedFiatUsd = order.fiatAmountUsd();
        BigDecimal plannedCryptoUsd = valuationService.cryptoToUsd(order.asset(), effectiveOnRampCryptoAmount(order));
        if (plannedCryptoUsd != null && confirmedFiatUsd != null && plannedCryptoUsd.compareTo(confirmedFiatUsd) > 0) {
            return RuleResult.block(RuleId.ON_RAMP_CONSERVATION, Decision.OPS_REVIEW,
                    Set.of(ReasonCode.PAYOUT_EXCEEDS_CONFIRMED_VALUE), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE,
                    List.of("plannedCryptoUsd=" + plannedCryptoUsd, "confirmedFiatUsd=" + confirmedFiatUsd));
        }
        return RuleResult.pass(RuleId.ON_RAMP_CONSERVATION);
    }

    /**
     * 校验链上确认数是否达到要求。
     *
     * @param order 当前订单
     * @param asset 资产网络配置
     * @return 确认数规则结果，非 off-ramp 时返回 null
     */
    private RuleResult evaluateConfirmation(OrderRecord order, AssetNetworkRecord asset) {
        if (order.type() != OrderType.OFF_RAMP || asset == null) {
            return null;
        }
        Integer confirmations = order.deposit() == null ? null : order.deposit().confirmations();
        if (confirmations == null) {
            return RuleResult.block(RuleId.CONFIRMATION, Decision.OPS_REVIEW,
                    Set.of(ReasonCode.CONFIRMATIONS_MISSING), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE, List.of("confirmations missing"));
        }
        if (confirmations < asset.confirmationsRequired()) {
            return RuleResult.block(RuleId.CONFIRMATION, Decision.TEMPORARY_HOLD,
                    Set.of(ReasonCode.INSUFFICIENT_CONFIRMATIONS), Set.of(EscalationTarget.OPS), Retryability.RETRYABLE,
                    List.of("confirmations=" + confirmations, "required=" + asset.confirmationsRequired()));
        }
        return RuleResult.pass(RuleId.CONFIRMATION);
    }

    /**
     * 校验到账数量与报价数量是否一致。
     *
     * @param order 当前订单
     * @return 数量一致性规则结果，非 off-ramp 时返回 null
     */
    private RuleResult evaluateAmountMatch(OrderRecord order) {
        if (order.type() != OrderType.OFF_RAMP || order.deposit() == null) {
            return null;
        }
        BigDecimal observed = order.deposit().observedAmount();
        BigDecimal quoted = order.quotedCryptoAmount();
        if (observed == null || quoted == null) {
            return RuleResult.block(RuleId.AMOUNT_MATCH, Decision.OPS_REVIEW,
                    Set.of(ReasonCode.REQUIRED_FACT_MISSING), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE, List.of("amount missing"));
        }
        if (observed.compareTo(quoted) != 0) {
            return RuleResult.block(RuleId.AMOUNT_MATCH, Decision.OPS_REVIEW,
                    Set.of(ReasonCode.AMOUNT_MISMATCH), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE,
                    List.of("observed=" + observed, "quoted=" + quoted));
        }
        return RuleResult.pass(RuleId.AMOUNT_MATCH);
    }

    /**
     * 校验法币出款是否超过已确认入账价值。
     *
     * @param order 当前订单
     * @param asset 资产网络配置
     * @return 出款守恒规则结果
     */
    private RuleResult evaluatePayoutConservation(OrderRecord order, AssetNetworkRecord asset) {
        if (order.type() != OrderType.OFF_RAMP || asset == null || order.deposit() == null || order.payout() == null) {
            return null;
        }
        BigDecimal confirmedIncomingUsd = valuationService.cryptoToUsd(order.asset(), order.deposit().observedAmount());
        BigDecimal payoutUsd = valuationService.fiatToUsd(order.payout().currency(), order.payout().amount());
        if (confirmedIncomingUsd != null && payoutUsd != null && payoutUsd.compareTo(confirmedIncomingUsd) > 0) {
            return RuleResult.block(RuleId.PAYOUT_CONSERVATION, Decision.OPS_REVIEW,
                    Set.of(ReasonCode.PAYOUT_EXCEEDS_CONFIRMED_VALUE), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE,
                    List.of("payoutUsd=" + payoutUsd, "confirmedIncomingUsd=" + confirmedIncomingUsd));
        }
        return RuleResult.pass(RuleId.PAYOUT_CONSERVATION);
    }

    /**
     * 校验声明网络与实际到账网络是否一致。
     *
     * @param order 当前订单
     * @return 网络一致性规则结果
     */
    private RuleResult evaluateNetworkMatch(OrderRecord order) {
        if (order.type() != OrderType.OFF_RAMP || order.deposit() == null || order.deposit().network() == null) {
            return null;
        }
        if (!order.network().equals(order.deposit().network())) {
            return RuleResult.block(RuleId.NETWORK_MATCH, Decision.OPS_REVIEW,
                    Set.of(ReasonCode.NETWORK_MISMATCH), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE,
                    List.of("declared=" + order.network(), "observed=" + order.deposit().network()));
        }
        return RuleResult.pass(RuleId.NETWORK_MATCH);
    }

    /**
     * 校验法币出款账户户名是否与客户实名一致。
     *
     * @param order 当前订单
     * @param customer 订单关联客户
     * @return 银行户名规则结果
     */
    private RuleResult evaluateBankOwnership(OrderRecord order, CustomerRecord customer) {
        if (order.type() != OrderType.OFF_RAMP || order.payout() == null) {
            return null;
        }
        if (order.payout().bankAccountName() == null || order.payout().bankAccountName().isBlank()) {
            return RuleResult.block(RuleId.BANK_OWNERSHIP, Decision.OPS_REVIEW,
                    Set.of(ReasonCode.BANK_ACCOUNT_NAME_MISSING), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE, List.of("bank name missing"));
        }
        if (customer != null && order.payout().bankAccountName().equals(customer.verifiedBankName())) {
            return RuleResult.pass(RuleId.BANK_OWNERSHIP);
        }
        return RuleResult.block(RuleId.BANK_OWNERSHIP, Decision.REJECT,
                Set.of(ReasonCode.BANK_NAME_MISMATCH), Set.of(EscalationTarget.COMPLIANCE), Retryability.NON_RETRYABLE,
                List.of("bank=" + order.payout().bankAccountName(), "verified=" + (customer == null ? null : customer.verifiedBankName())));
    }

    /**
     * 校验报价是否过期，以及过期后滑点是否仍在容忍范围内。
     *
     * @param order 当前订单
     * @param asset 资产网络配置
     * @return 报价过期规则结果
     */
    private RuleResult evaluateQuoteExpiry(OrderRecord order, AssetNetworkRecord asset) {
        if (order.type() == OrderType.WITHDRAWAL || order.quoteExpiresAt() == null || asset == null) {
            return null;
        }
        if (clock.instant().isBefore(order.quoteExpiresAt())) {
            return RuleResult.pass(RuleId.QUOTE_EXPIRY);
        }
        BigDecimal quoted = order.quotedCryptoAmount();
        if (quoted == null) {
            return RuleResult.block(RuleId.QUOTE_EXPIRY, Decision.OPS_REVIEW,
                    Set.of(ReasonCode.QUOTE_MISSING), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE, List.of("quote missing"));
        }
        BigDecimal referenceRate = valuationService.cryptoToUsd(order.asset(), BigDecimal.ONE);
        if (referenceRate == null) {
            return RuleResult.block(RuleId.QUOTE_EXPIRY, Decision.TEMPORARY_HOLD,
                    Set.of(ReasonCode.REFERENCE_RATE_MISSING), Set.of(EscalationTarget.OPS), Retryability.RETRYABLE, List.of("reference rate missing"));
        }
        BigDecimal newCryptoAmount;
        if (order.type() == OrderType.ON_RAMP && order.fiatAmountUsd() != null) {
            newCryptoAmount = order.fiatAmountUsd().divide(referenceRate, MathContext.DECIMAL128);
        } else if (order.type() == OrderType.OFF_RAMP && order.deposit() != null && order.deposit().observedAmount() != null) {
            newCryptoAmount = order.deposit().observedAmount();
        } else {
            return RuleResult.block(RuleId.QUOTE_EXPIRY, Decision.TEMPORARY_HOLD,
                    Set.of(ReasonCode.REQUIRED_FACT_MISSING), Set.of(EscalationTarget.OPS), Retryability.RETRYABLE, List.of("missing quote inputs"));
        }
        BigDecimal slippage = newCryptoAmount.subtract(quoted, MathContext.DECIMAL128)
                .abs()
                .divide(quoted, MathContext.DECIMAL128);
        if (slippage.compareTo(new BigDecimal("0.01")) > 0) {
            return RuleResult.block(RuleId.QUOTE_EXPIRY, Decision.REQUOTE,
                    Set.of(ReasonCode.QUOTE_SLIPPAGE_EXCEEDED), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE,
                    List.of("slippage=" + slippage));
        }
        return RuleResult.pass(RuleId.QUOTE_EXPIRY);
    }

    /**
     * 校验大额跨机构转移是否满足 Travel Rule 要求。
     *
     * @param order 当前订单
     * @param asset 资产网络配置
     * @return Travel Rule 规则结果
     */
    private RuleResult evaluateTravelRule(OrderRecord order, AssetNetworkRecord asset) {
        if (!isCryptoTransfer(order) || asset == null || order.counterparty() == null || !order.counterparty().isVasp()) {
            return null;
        }
        BigDecimal transferAmount = switch (order.type()) {
            case ON_RAMP -> order.quotedCryptoAmount();
            case OFF_RAMP -> order.deposit() == null ? null : order.deposit().observedAmount();
            case WITHDRAWAL -> order.amount();
        };
        BigDecimal usd = valuationService.cryptoToUsd(order.asset(), transferAmount);
        if (usd == null || usd.compareTo(TRAVEL_RULE_THRESHOLD_USD) < 0) {
            return null;
        }
        if (order.counterparty().beneficiaryInfo() == null) {
            return RuleResult.block(RuleId.TRAVEL_RULE, Decision.COMPLIANCE_HOLD,
                    Set.of(ReasonCode.TRAVEL_RULE_INFO_MISSING), Set.of(EscalationTarget.COMPLIANCE), Retryability.NON_RETRYABLE,
                    List.of("travel rule info missing"));
        }
        return RuleResult.pass(RuleId.TRAVEL_RULE);
    }

    /**
     * 校验提币资金是否已被系统确认。
     *
     * @param order 当前订单
     * @return 提币资金规则结果
     */
    private RuleResult evaluateWithdrawalFunds(OrderRecord order) {
        if (order.type() != OrderType.WITHDRAWAL) {
            return null;
        }
        return RuleResult.block(RuleId.WITHDRAWAL_FUNDS, Decision.OPS_REVIEW,
                Set.of(ReasonCode.WITHDRAWAL_FUNDS_UNVERIFIED), Set.of(EscalationTarget.OPS), Retryability.NON_RETRYABLE,
                List.of("wallet funds unverified"));
    }

    /**
     * 对未知 VASP 名称给出提示性告警。
     *
     * @param order 当前订单
     * @param asset 资产网络配置
     * @return VASP 告警规则结果
     */
    private RuleResult evaluateVaspUnknownWarning(OrderRecord order, AssetNetworkRecord asset) {
        if (!isCryptoTransfer(order) || asset == null || order.counterparty() == null || !order.counterparty().isVasp()) {
            return null;
        }
        BigDecimal transferAmount = switch (order.type()) {
            case ON_RAMP -> order.quotedCryptoAmount();
            case OFF_RAMP -> order.deposit() == null ? null : order.deposit().observedAmount();
            case WITHDRAWAL -> order.amount();
        };
        BigDecimal usd = valuationService.cryptoToUsd(order.asset(), transferAmount);
        if (usd == null || usd.compareTo(TRAVEL_RULE_THRESHOLD_USD) < 0) {
            return null;
        }
        String vaspName = order.counterparty().vaspName();
        if (vaspName == null || vaspName.isBlank() || "unknown".equalsIgnoreCase(vaspName)) {
            return RuleResult.warn(RuleId.VASP_UNKNOWN_WARNING,
                    Set.of(ReasonCode.VASP_STATUS_UNKNOWN), Set.of(EscalationTarget.COMPLIANCE), List.of("vasp unknown"));
        }
        return RuleResult.pass(RuleId.VASP_UNKNOWN_WARNING);
    }

    /**
     * 判断订单是否属于加密资产转移类订单。
     *
     * @param order 当前订单
     * @return 是否为加密资产转移
     */
    private static boolean isCryptoTransfer(OrderRecord order) {
        return order.type() == OrderType.ON_RAMP || order.type() == OrderType.OFF_RAMP || order.type() == OrderType.WITHDRAWAL;
    }

    /**
     * 计算 on-ramp 在报价过期后的有效加密资产数量。
     *
     * @param order 当前订单
     * @return 有效加密资产数量
     */
    /**
     * 计算 on-ramp 在报价过期后的有效加密资产数量。
     *
     * @param order 当前订单
     * @return 有效加密资产数量
     */
    private BigDecimal effectiveOnRampCryptoAmount(OrderRecord order) {
        if (order.quoteExpiresAt() != null && clock.instant().isAfter(order.quoteExpiresAt())) {
            BigDecimal rate = valuationService.cryptoToUsd(order.asset(), BigDecimal.ONE);
            if (rate != null && order.fiatAmountUsd() != null) {
                return order.fiatAmountUsd().divide(rate, MathContext.DECIMAL128);
            }
        }
        return order.quotedCryptoAmount();
    }

    /**
     * 取两个金额中的较大值，空值按另一个值处理。
     *
     * @param left 左侧金额
     * @param right 右侧金额
     * @return 较大金额
     */
    /**
     * 取两个金额中的较大值，空值按另一个值处理。
     *
     * @param left 左侧金额
     * @param right 右侧金额
     * @return 较大金额
     */
    private static BigDecimal max(BigDecimal left, BigDecimal right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.compareTo(right) >= 0 ? left : right;
    }
}
