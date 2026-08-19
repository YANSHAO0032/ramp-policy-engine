package com.ramppolicy.engine.domain;

/**
 * 出金对手方元数据。
 *
 * @param isVasp 对手方是否为 VASP
 * @param vaspName VASP 名称，未知时可为空或填入 unknown
 * @param beneficiaryInfo 受益人信息负载，题目 Demo 中非空即视为存在
 */
public record CounterpartyRecord(
        boolean isVasp,
        String vaspName,
        Object beneficiaryInfo) {
}
