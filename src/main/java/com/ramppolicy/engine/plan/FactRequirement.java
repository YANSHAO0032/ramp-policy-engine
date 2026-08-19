package com.ramppolicy.engine.plan;

/**
 * Structured fact buckets collected before policy rules run.
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
