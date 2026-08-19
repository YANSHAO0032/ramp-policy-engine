package com.ramppolicy.engine.idempotency;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory transaction-level idempotency store for off-ramp deposits.
 */
public final class InMemoryTransactionIdempotencyStore {

    private final ConcurrentMap<String, String> owners = new ConcurrentHashMap<>();

    /**
     * Claims a transaction fingerprint for a caller or attempt owner.
     *
     * @param asset asset ticker
     * @param network network code
     * @param txHash transaction hash
     * @param owner owner or attempt identifier
     * @return claim outcome
     */
    public ClaimResult claim(String asset, String network, String txHash, String owner) {
        String key = asset + "/" + network + "/" + txHash;
        String existing = owners.putIfAbsent(key, owner);
        if (existing == null || Objects.equals(existing, owner)) {
            return ClaimResult.acceptedClaim();
        }
        return ClaimResult.duplicateClaim();
    }
}
