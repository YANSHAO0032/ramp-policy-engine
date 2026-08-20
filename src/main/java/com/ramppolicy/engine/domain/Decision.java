package com.ramppolicy.engine.domain;

/**
 * 确定性策略引擎输出的顶层处置结果。
 */
public enum Decision {
    /** 所有强制规则通过，可继续自动处理。 */
    COMPLETE,
    /** 需要等待可重试的事实补齐后再继续。 */
    TEMPORARY_HOLD,
    /** 需要重新报价后再继续。 */
    REQUOTE,
    /** 需要运维人工复核。 */
    OPS_REVIEW,
    /** 需要合规人工复核。 */
    COMPLIANCE_HOLD,
    /** 订单被确定性地拒绝。 */
    REJECT,
    /** 命中高风险冻结条件。 */
    FREEZE
}
