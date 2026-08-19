package com.ramppolicy.engine.domain;

import java.math.BigDecimal;

/**
 * Asset/network configuration from the demo data source.
 *
 * @param asset asset ticker
 * @param network network code
 * @param minAmount minimum supported amount
 * @param confirmationsRequired required confirmations for deposits
 */
public record AssetNetworkRecord(
        String asset,
        String network,
        BigDecimal minAmount,
        int confirmationsRequired) {
}
