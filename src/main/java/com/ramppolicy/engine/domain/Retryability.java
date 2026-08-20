package com.ramppolicy.engine.domain;

/**
 * 当前处置结果是否允许安全自动重试。
 */
public enum Retryability {
    /** 当前结果与重试无关。 */
    NOT_APPLICABLE,
    /** 允许在事实补齐后重试。 */
    RETRYABLE,
    /** 不应自动重试。 */
    NON_RETRYABLE
}
