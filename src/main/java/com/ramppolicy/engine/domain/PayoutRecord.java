package com.ramppolicy.engine.domain;

import java.math.BigDecimal;

/**
 * 加密资产换法币订单的法币出款事实。
 *
 * @param bankAccountName 法币收款银行账户户名
 * @param currency 法币出款币种
 * @param amount 法币出款金额
 */
public record PayoutRecord(
        String bankAccountName,
        String currency,
        BigDecimal amount) {
}
