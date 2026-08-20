package com.ramppolicy.engine.plan;

/**
 * 确定性策略规则的唯一标识。
 */
public enum RuleId {
    /** 校验客户状态。 */
    CUSTOMER_STATUS,
    /** 校验资产与网络是否受支持。 */
    ASSET_SUPPORT,
    /** 校验地址风险。 */
    ADDRESS_RISK,
    /** 校验 KYC 限额。 */
    KYC_LIMIT,
    /** 校验最小金额。 */
    MINIMUM_AMOUNT,
    /** 校验法币入账状态。 */
    FIAT_RECEIPT,
    /** 校验 on-ramp 入金守恒。 */
    ON_RAMP_CONSERVATION,
    /** 校验链上确认数。 */
    CONFIRMATION,
    /** 校验到账数量与报价数量是否一致。 */
    AMOUNT_MATCH,
    /** 校验出款守恒。 */
    PAYOUT_CONSERVATION,
    /** 校验声明网络与实际网络是否一致。 */
    NETWORK_MATCH,
    /** 校验银行户名一致性。 */
    BANK_OWNERSHIP,
    /** 校验报价是否过期。 */
    QUOTE_EXPIRY,
    /** 校验 Travel Rule 要求。 */
    TRAVEL_RULE,
    /** 校验提币资金是否已确认。 */
    WITHDRAWAL_FUNDS,
    /** 对未知 VASP 名称给出告警。 */
    VASP_UNKNOWN_WARNING
}
