package com.ramppolicy.engine.domain;

import java.math.BigDecimal;

/**
 * Demo 事实源中的客户资料。
 *
 * @param id 客户标识
 * @param name 已验证客户姓名
 * @param kycTier 客户 KYC 等级
 * @param monthlyLimitUsd 以 USD 计价的月度 KYC 限额
 * @param verifiedBankName 已验证的银行账户姓名或收款主体名称
 * @param status 客户状态
 */
public record CustomerRecord(
        String id,
        String name,
        int kycTier,
        BigDecimal monthlyLimitUsd,
        String verifiedBankName,
        String status) {
}
