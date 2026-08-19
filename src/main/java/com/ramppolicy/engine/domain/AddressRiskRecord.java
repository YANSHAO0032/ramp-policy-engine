package com.ramppolicy.engine.domain;

/**
 * Address risk lookup payload.
 *
 * @param riskScore risk score from the source file
 * @param category risk category from the source file
 */
public record AddressRiskRecord(int riskScore, String category) {
}
