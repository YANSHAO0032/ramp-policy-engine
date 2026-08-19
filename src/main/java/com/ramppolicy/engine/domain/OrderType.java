package com.ramppolicy.engine.domain;

/**
 * The supported order directions in the demo policy engine.
 */
public enum OrderType {
    /**
     * Customer pays fiat and receives crypto to an external address.
     */
    ON_RAMP,

    /**
     * Customer deposits crypto and receives fiat to a bank account.
     */
    OFF_RAMP,

    /**
     * Customer withdraws crypto from the platform to an external address.
     */
    WITHDRAWAL
}
