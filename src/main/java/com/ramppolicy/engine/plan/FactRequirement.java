package com.ramppolicy.engine.plan;

/**
 * 策略规则执行前需要收集的结构化事实类别。
 */
public enum FactRequirement {
    /** 客户基础信息。 */
    CUSTOMER,
    /** 资产与网络配置。 */
    ASSET_CONFIG,
    /** 地址风险事实。 */
    COUNTERPARTY_ADDRESS_RISK,
    /** 法币入账事实。 */
    FIAT_RECEIPT,
    /** 报价事实。 */
    QUOTE,
    /** 参考汇率事实。 */
    REFERENCE_RATE,
    /** 链上入账事实。 */
    DEPOSIT,
    /** 法币出款事实。 */
    PAYOUT,
    /** 银行身份校验事实。 */
    BANK_IDENTITY,
    /** 对手方 VASP 信息。 */
    COUNTERPARTY_VASP,
    /** 钱包资金确认事实。 */
    WALLET_FUNDS
}
