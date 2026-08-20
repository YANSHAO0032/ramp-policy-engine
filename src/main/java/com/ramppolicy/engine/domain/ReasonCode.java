package com.ramppolicy.engine.domain;

/**
 * 策略决策的结构化机器可读原因码。
 */
public enum ReasonCode {
    /** 订单被判定为重复提交。 */
    DUPLICATE_ORDER,
    /** 订单仍在处理中。 */
    ORDER_ALREADY_PROCESSING,
    /** 订单标识缺失。 */
    ORDER_ID_MISSING,
    /** 客户信息缺失。 */
    CUSTOMER_MISSING,
    /** 客户未找到。 */
    CUSTOMER_NOT_FOUND,
    /** 客户当前不是可处理状态。 */
    CUSTOMER_NOT_ACTIVE,
    /** 交易金额超过客户 KYC 限额。 */
    KYC_LIMIT_EXCEEDED,
    /** 资产信息缺失。 */
    ASSET_MISSING,
    /** 网络信息缺失。 */
    NETWORK_MISSING,
    /** 资产不受支持。 */
    UNSUPPORTED_ASSET,
    /** 网络不受支持。 */
    UNSUPPORTED_NETWORK,
    /** 声明网络与实际网络不一致。 */
    NETWORK_MISMATCH,
    /** 金额低于最小阈值。 */
    BELOW_MIN_AMOUNT,
    /** 来源地址缺失。 */
    SOURCE_ADDRESS_MISSING,
    /** 目标地址缺失。 */
    DESTINATION_ADDRESS_MISSING,
    /** 地址命中制裁名单。 */
    ADDRESS_SANCTIONED,
    /** 地址命中高风险分类。 */
    ADDRESS_HIGH_RISK,
    /** 地址无法识别。 */
    ADDRESS_UNKNOWN,
    /** 地址风险查询失败。 */
    ADDRESS_RISK_LOOKUP_FAILED,
    /** 法币入金状态缺失。 */
    FIAT_STATUS_MISSING,
    /** 法币尚未到账。 */
    FIAT_NOT_RECEIVED,
    /** 法币支付失败。 */
    FIAT_PAYMENT_FAILED,
    /** 法币支付被冲正。 */
    FIAT_PAYMENT_REVERSED,
    /** 报价信息缺失。 */
    QUOTE_MISSING,
    /** 报价已过期。 */
    QUOTE_EXPIRED,
    /** 报价滑点超出允许范围。 */
    QUOTE_SLIPPAGE_EXCEEDED,
    /** 参考汇率缺失。 */
    REFERENCE_RATE_MISSING,
    /** 交易被判定为重复。 */
    DUPLICATE_TRANSACTION,
    /** 入账明细缺失。 */
    DEPOSIT_MISSING,
    /** 交易哈希缺失。 */
    TX_HASH_MISSING,
    /** 链上确认数缺失。 */
    CONFIRMATIONS_MISSING,
    /** 链上确认数不足。 */
    INSUFFICIENT_CONFIRMATIONS,
    /** 实际到账金额缺失。 */
    OBSERVED_AMOUNT_MISSING,
    /** 实际到账金额与报价金额不一致。 */
    AMOUNT_MISMATCH,
    /** 出款信息缺失。 */
    PAYOUT_MISSING,
    /** 出款金额超过已确认入账价值。 */
    PAYOUT_EXCEEDS_CONFIRMED_VALUE,
    /** 银行户名缺失。 */
    BANK_ACCOUNT_NAME_MISSING,
    /** 银行户名与实名不一致。 */
    BANK_NAME_MISMATCH,
    /** 幂等性操作失败。 */
    IDEMPOTENCY_OPERATION_FAILED,
    /** 对手方 VASP 状态未知。 */
    VASP_STATUS_UNKNOWN,
    /** Travel Rule 所需信息缺失。 */
    TRAVEL_RULE_INFO_MISSING,
    /** 提币资金尚未被系统确认。 */
    WITHDRAWAL_FUNDS_UNVERIFIED,
    /** 必要事实缺失。 */
    REQUIRED_FACT_MISSING,
    /** 工具失败且可重试。 */
    TOOL_FAILURE_RETRYABLE,
    /** 工具失败且不可重试。 */
    TOOL_FAILURE_NON_RETRYABLE,
    /** 订单数据无效。 */
    INVALID_ORDER_DATA,
    /** 订单类型未知。 */
    UNKNOWN_ORDER_TYPE,
    /** 订单处理超时。 */
    ORDER_PROCESSING_TIMEOUT
}
