package com.ramppolicy.engine.domain;

import java.math.BigDecimal;

/**
 * Customer facts from the demo data source.
 *
 * @param id customer identifier
 * @param name verified customer name
 * @param kycTier assigned KYC tier
 * @param monthlyLimitUsd monthly KYC limit in USD
 * @param verifiedBankName bank name or beneficiary name verified for payouts
 * @param status customer status
 */
public record CustomerRecord(
        String id,
        String name,
        int kycTier,
        BigDecimal monthlyLimitUsd,
        String verifiedBankName,
        String status) {
}
