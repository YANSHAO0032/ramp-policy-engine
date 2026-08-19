package com.ramppolicy.engine.domain;

/**
 * 当前处置结果是否允许安全自动重试。
 */
public enum Retryability {
    NOT_APPLICABLE,
    RETRYABLE,
    NON_RETRYABLE
}
