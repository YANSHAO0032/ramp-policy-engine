package com.ramppolicy.engine.domain;

import java.math.BigDecimal;

/**
 * Deposit facts for off-ramp orders.
 *
 * @param txHash transaction hash
 * @param fromAddress source address
 * @param confirmations observed confirmations
 * @param observedAmount observed crypto amount
 * @param network observed network, if present
 */
public record DepositRecord(
        String txHash,
        String fromAddress,
        Integer confirmations,
        BigDecimal observedAmount,
        String network) {
}
