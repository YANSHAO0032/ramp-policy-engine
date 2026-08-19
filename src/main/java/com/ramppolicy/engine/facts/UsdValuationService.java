package com.ramppolicy.engine.facts;

import com.ramppolicy.engine.domain.BigDecimals;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Converts demo asset and fiat amounts into USD using reference rates.
 */
public final class UsdValuationService {

    private final Map<String, BigDecimal> referenceRates;

    public UsdValuationService(Map<String, BigDecimal> referenceRates) {
        this.referenceRates = Map.copyOf(referenceRates);
    }

    /**
     * Converts a crypto amount to USD using the reference-rate table.
     *
     * @param asset asset ticker
     * @param amount crypto amount
     * @return USD value, or null when the rate is unavailable
     */
    public BigDecimal cryptoToUsd(String asset, BigDecimal amount) {
        BigDecimal rate = referenceRates.get(asset + "/USD");
        if (rate == null || amount == null) {
            return null;
        }
        return amount.multiply(rate, BigDecimals.MONEY_CONTEXT);
    }

    /**
     * Converts a fiat amount to USD using the reference-rate table.
     *
     * @param currency fiat currency
     * @param amount fiat amount
     * @return USD value, or null when the rate is unavailable
     */
    public BigDecimal fiatToUsd(String currency, BigDecimal amount) {
        if ("USD".equals(currency)) {
            return amount;
        }
        BigDecimal rate = referenceRates.get(currency + "/USD");
        if (rate == null || amount == null) {
            return null;
        }
        return amount.multiply(rate, BigDecimals.MONEY_CONTEXT);
    }
}
