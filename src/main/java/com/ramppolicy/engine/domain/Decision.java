package com.ramppolicy.engine.domain;

/**
 * Deterministic top-level policy outcome.
 */
public enum Decision {
    COMPLETE,
    TEMPORARY_HOLD,
    REQUOTE,
    OPS_REVIEW,
    COMPLIANCE_HOLD,
    REJECT,
    FREEZE
}
