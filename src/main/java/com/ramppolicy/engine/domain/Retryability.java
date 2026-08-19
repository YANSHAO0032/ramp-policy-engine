package com.ramppolicy.engine.domain;

/**
 * Whether the workflow can be safely retried.
 */
public enum Retryability {
    NOT_APPLICABLE,
    RETRYABLE,
    NON_RETRYABLE
}
