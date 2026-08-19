package com.ramppolicy.engine.domain;

/**
 * Withdrawal counterparty metadata.
 *
 * @param isVasp whether the counterparty is a VASP
 * @param vaspName VASP name, if known
 * @param beneficiaryInfo beneficiary info payload, if any
 */
public record CounterpartyRecord(
        boolean isVasp,
        String vaspName,
        Object beneficiaryInfo) {
}
