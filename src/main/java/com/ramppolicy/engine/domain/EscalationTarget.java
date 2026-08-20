package com.ramppolicy.engine.domain;

/**
 * 订单需要升级流转的人工复核团队。
 */
public enum EscalationTarget {
    /** 需要运维团队处理。 */
    OPS,
    /** 需要合规团队处理。 */
    COMPLIANCE
}
