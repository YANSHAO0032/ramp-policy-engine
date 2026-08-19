package com.ramppolicy.engine.domain;

/**
 * 地址风险库中的查询结果。
 *
 * @param riskScore 风险库给出的风险分数
 * @param category 风险库给出的风险类别
 */
public record AddressRiskRecord(int riskScore, String category) {
}
