package com.ramppolicy.engine.idempotency;

/**
 * 幂等键占用操作的结果。
 *
 * @param accepted 当前调用方是否成功持有或可恢复该占用
 * @param duplicate 是否已有其他持有方占用同一幂等键
 */
public record ClaimResult(boolean accepted, boolean duplicate) {

    public static ClaimResult acceptedClaim() {
        return new ClaimResult(true, false);
    }

    /**
     * 创建因其他持有方已占用幂等键而被拒绝的结果。
     *
     * @return 重复占用结果
     */
    public static ClaimResult duplicateClaim() {
        return new ClaimResult(false, true);
    }
}
