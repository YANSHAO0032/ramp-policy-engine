package com.ramppolicy.engine.domain;

import java.math.BigDecimal;

/**
 * Demo 事实源中的资产和网络配置。
 *
 * @param asset 资产代码
 * @param network 网络代码
 * @param minAmount 该资产网络支持的最小加密资产数量
 * @param confirmationsRequired 加密资产换法币订单的入金需要达到的链上确认数
 */
public record AssetNetworkRecord(
        String asset,
        String network,
        BigDecimal minAmount,
        int confirmationsRequired) {
}
