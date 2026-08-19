package com.ramppolicy.engine.idempotency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryIdempotencyStoreTest {

    @Test
    void sameOwnerCanResumeProcessingButOtherOwnerCannotStealOrder() {
        InMemoryOrderIdempotencyStore store = new InMemoryOrderIdempotencyStore();

        assertTrue(store.claim("O-001", "worker-a").accepted());
        assertTrue(store.claim("O-001", "worker-a").accepted());
        assertFalse(store.claim("O-001", "worker-b").accepted());
    }

    @Test
    void duplicateTransactionCannotBeClaimedByAnotherOrder() {
        InMemoryTransactionIdempotencyStore store = new InMemoryTransactionIdempotencyStore();

        assertTrue(store.claim("USDT", "TRC20", "0xa1", "O-002").accepted());
        assertTrue(store.claim("USDT", "TRC20", "0xa1", "O-002").accepted());
        assertFalse(store.claim("USDT", "TRC20", "0xa1", "O-013").accepted());
    }
}
