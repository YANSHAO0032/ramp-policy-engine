package com.ramppolicy.engine.plan;

/**
 * 策略规则执行前需要收集的结构化事实类别。
 */
public enum FactRequirement {
    CUSTOMER,
    ASSET_CONFIG,
    COUNTERPARTY_ADDRESS_RISK,
    FIAT_RECEIPT,
    QUOTE,
    REFERENCE_RATE,
    DEPOSIT,
    PAYOUT,
    BANK_IDENTITY,
    COUNTERPARTY_VASP,
    WALLET_FUNDS
}
