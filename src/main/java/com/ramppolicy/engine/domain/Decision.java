package com.ramppolicy.engine.domain;

/**
 * 确定性策略引擎输出的顶层处置结果。
 */
public enum Decision {
    COMPLETE,
    TEMPORARY_HOLD,
    REQUOTE,
    OPS_REVIEW,
    COMPLIANCE_HOLD,
    REJECT,
    FREEZE
}
