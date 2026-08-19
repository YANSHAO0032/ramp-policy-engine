package com.ramppolicy.engine.idempotency;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 加密资产换法币链上入金凭证使用的内存级交易幂等存储。
 */
public final class InMemoryTransactionIdempotencyStore {

    private final ConcurrentMap<String, String> owners = new ConcurrentHashMap<>();

    /**
     * 为某个处理方原子占用链上交易指纹。
     *
     * @param asset 资产代码
     * @param network 网络代码
     * @param txHash 链上交易哈希
     * @param owner 当前处理方或尝试标识
     * @return 占用结果
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
