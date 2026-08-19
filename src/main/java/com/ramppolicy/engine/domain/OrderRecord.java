package com.ramppolicy.engine.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 从 Demo JSONL 文件解析得到的订单。
 *
 * @param orderId 订单标识
 * @param type 订单类型
 * @param customerId 客户标识
 * @param asset 资产代码
 * @param network 网络代码
 * @param fiatAmountUsd 法币换加密资产订单的 USD 法币金额
 * @param quotedCryptoAmount 报价或预期转账的加密资产数量
 * @param quoteExpiresAt 报价过期时间
 * @param fiatStatus 法币换加密资产订单的法币收款状态
 * @param destinationAddress 加密资产出金目标地址
 * @param deposit 加密资产换法币订单的链上入金事实
 * @param payout 加密资产换法币订单的法币出款事实
 * @param amount 提币订单的提币数量
 * @param counterparty 提币订单的对手方信息
 * @param customerNote 客户自由文本备注，永远不是权威控制指令
 */
public record OrderRecord(
        String orderId,
        OrderType type,
        String customerId,
        String asset,
        String network,
        BigDecimal fiatAmountUsd,
        BigDecimal quotedCryptoAmount,
        Instant quoteExpiresAt,
        String fiatStatus,
        String destinationAddress,
        DepositRecord deposit,
        PayoutRecord payout,
        BigDecimal amount,
        CounterpartyRecord counterparty,
        String customerNote) {
}
