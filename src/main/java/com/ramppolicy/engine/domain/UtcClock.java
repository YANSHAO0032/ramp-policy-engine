package com.ramppolicy.engine.domain;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * 统一 UTC 时钟工具，用于保持评估过程可复现。
 */
public final class UtcClock {

    private UtcClock() {
    }

    public static Clock fixed(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }

    /**
     * 返回非 Demo 场景可使用的系统 UTC 时钟。
     *
     * @return UTC 时钟
     */
    public static Clock utc() {
        return Clock.systemUTC();
    }
}
