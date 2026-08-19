package com.ramppolicy.engine.domain;

import java.math.BigDecimal;

/**
 * 加密资产换法币订单的链上入金事实。
 *
 * @param txHash 链上交易哈希
 * @param fromAddress 入金来源地址
 * @param confirmations 已观察到的链上确认数
 * @param observedAmount 已观察到的加密资产入金数量
 * @param network 链上实际观察到的网络，若题目数据提供则使用
 */
public record DepositRecord(
        String txHash,
        String fromAddress,
        Integer confirmations,
        BigDecimal observedAmount,
        String network) {
}
