package com.ramppolicy.engine.idempotency;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory order-level idempotency store for the demo runner.
 */
public final class InMemoryOrderIdempotencyStore {

    private final ConcurrentMap<String, String> owners = new ConcurrentHashMap<>();

    /**
     * Claims the given order identifier for a caller or attempt owner.
     *
     * @param orderId order identifier
     * @param owner owner or attempt identifier
     * @return claim outcome
     */
    public ClaimResult claim(String orderId, String owner) {
        String existing = owners.putIfAbsent(orderId, owner);
        if (existing == null || Objects.equals(existing, owner)) {
            return ClaimResult.acceptedClaim();
        }
        return ClaimResult.duplicateClaim();
    }
}
