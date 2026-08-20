package com.ramppolicy.engine.policy;

/**
 * 单条策略规则在确定性引擎中的执行结果。
 */
public enum RuleOutcome {
    /** 规则通过。 */
    PASS,
    /** 规则提示告警，但不阻断。 */
    WARN,
    /** 规则阻断流程。 */
    BLOCK
}
