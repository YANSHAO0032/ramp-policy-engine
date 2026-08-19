package com.ramppolicy.engine.domain;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Central UTC clock helper for reproducible evaluation.
 */
public final class UtcClock {

    private UtcClock() {
    }

    public static Clock fixed(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }

    /**
     * Returns the system UTC clock for non-demo runs.
     *
     * @return UTC clock
     */
    public static Clock utc() {
        return Clock.systemUTC();
    }
}
