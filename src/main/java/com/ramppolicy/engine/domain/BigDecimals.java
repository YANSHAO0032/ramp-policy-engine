package com.ramppolicy.engine.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Shared decimal helpers for monetary and ratio calculations.
 */
public final class BigDecimals {

    /**
     * Preferred precision for money and valuation math in the demo engine.
     */
    public static final MathContext MONEY_CONTEXT = MathContext.DECIMAL128;

    private BigDecimals() {
    }

    /**
     * Parses a decimal string with the demo money precision.
     *
     * @param value decimal string
     * @return parsed decimal
     */
    public static BigDecimal of(String value) {
        return new BigDecimal(value, MONEY_CONTEXT);
    }

    /**
     * Divides two values using the shared money context.
     *
     * @param dividend numerator
     * @param divisor denominator
     * @return quotient
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        return dividend.divide(divisor, MONEY_CONTEXT);
    }

    /**
     * Computes a percentage from a part and whole.
     *
     * @param part numerator
     * @param whole denominator
     * @return percentage value
     */
    public static BigDecimal percent(BigDecimal part, BigDecimal whole) {
        return part.divide(whole, MONEY_CONTEXT).multiply(BigDecimal.valueOf(100), MONEY_CONTEXT);
    }

    /**
     * Checks whether a value is negative.
     *
     * @param value value to check
     * @return true when negative
     */
    public static boolean isNegative(BigDecimal value) {
        return value != null && value.signum() < 0;
    }

    /**
     * Checks whether the left value strictly exceeds the right value.
     *
     * @param left left-hand value
     * @param right right-hand value
     * @return true when left exceeds right
     */
    public static boolean exceeds(BigDecimal left, BigDecimal right) {
        return left != null && right != null && left.compareTo(right) > 0;
    }
}
