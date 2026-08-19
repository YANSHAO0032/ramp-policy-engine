package com.ramppolicy.engine.idempotency;

/**
 * Result of an idempotency claim attempt.
 *
 * @param accepted whether the caller owns or can resume the claim
 * @param duplicate whether another owner already claimed the same key
 */
public record ClaimResult(boolean accepted, boolean duplicate) {

    public static ClaimResult acceptedClaim() {
        return new ClaimResult(true, false);
    }

    /**
     * Creates a rejected claim for a key already owned elsewhere.
     *
     * @return duplicate claim result
     */
    public static ClaimResult duplicateClaim() {
        return new ClaimResult(false, true);
    }
}
