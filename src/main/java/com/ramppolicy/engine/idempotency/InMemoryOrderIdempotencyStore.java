package com.ramppolicy.engine.idempotency;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Demo 运行器使用的内存级订单幂等存储。
 */
public final class InMemoryOrderIdempotencyStore {

    private final ConcurrentMap<String, String> owners = new ConcurrentHashMap<>();

    /**
     * 为某个处理方原子占用指定订单标识。
     *
     * @param orderId 订单标识
     * @param owner 当前处理方或尝试标识
     * @return 占用结果
     */
    public ClaimResult claim(String orderId, String owner) {
        String existing = owners.putIfAbsent(orderId, owner);
        if (existing == null || Objects.equals(existing, owner)) {
            return ClaimResult.acceptedClaim();
        }
        return ClaimResult.duplicateClaim();
    }
}
