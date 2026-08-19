package com.ramppolicy.engine.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolicyVersionTest {

    @Test
    void usesPinnedPolicyVersion() {
        assertEquals("MSB-V4", PolicyVersion.VALUE);
    }
}
