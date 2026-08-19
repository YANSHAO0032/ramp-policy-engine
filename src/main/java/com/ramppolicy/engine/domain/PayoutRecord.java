package com.ramppolicy.engine.domain;

import java.math.BigDecimal;

/**
 * Fiat payout facts for off-ramp orders.
 *
 * @param bankAccountName payout bank account holder name
 * @param currency payout currency
 * @param amount payout amount
 */
public record PayoutRecord(
        String bankAccountName,
        String currency,
        BigDecimal amount) {
}
