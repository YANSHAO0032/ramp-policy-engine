package com.ramppolicy.engine.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Parsed order from the JSONL demo file.
 *
 * @param orderId order identifier
 * @param type order type
 * @param customerId customer identifier
 * @param asset asset ticker
 * @param network network code
 * @param fiatAmountUsd fiat amount for on-ramp orders
 * @param quotedCryptoAmount quoted or transferred crypto amount
 * @param quoteExpiresAt quote expiration instant
 * @param fiatStatus on-ramp fiat receipt status
 * @param destinationAddress destination address for crypto transfers
 * @param deposit deposit payload for off-ramp orders
 * @param payout payout payload for off-ramp orders
 * @param amount withdrawal amount
 * @param counterparty counterparty payload for withdrawal orders
 * @param customerNote free-text customer note, never authoritative
 */
public record OrderRecord(
        String orderId,
        OrderType type,
        String customerId,
        String asset,
        String network,
        BigDecimal fiatAmountUsd,
        BigDecimal quotedCryptoAmount,
        Instant quoteExpiresAt,
        String fiatStatus,
        String destinationAddress,
        DepositRecord deposit,
        PayoutRecord payout,
        BigDecimal amount,
        CounterpartyRecord counterparty,
        String customerNote) {
}
