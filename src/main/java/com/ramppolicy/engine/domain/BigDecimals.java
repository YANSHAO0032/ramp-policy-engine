package com.ramppolicy.engine.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * 金额、估值和比例计算使用的 BigDecimal 工具。
 */
public final class BigDecimals {

    /**
     * Demo 引擎中金额和估值计算统一使用的精度。
     */
    public static final MathContext MONEY_CONTEXT = MathContext.DECIMAL128;

    private BigDecimals() {
    }

    /**
     * 使用统一金额精度解析十进制字符串。
     *
     * @param value 十进制字符串
     * @return 解析后的数值
     */
    public static BigDecimal of(String value) {
        return new BigDecimal(value, MONEY_CONTEXT);
    }

    /**
     * 使用统一金额精度执行除法。
     *
     * @param dividend 被除数
     * @param divisor 除数
     * @return 商
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        return dividend.divide(divisor, MONEY_CONTEXT);
    }

    /**
     * 根据部分值和整体值计算百分比。
     *
     * @param part 部分值
     * @param whole 整体值
     * @return 百分比数值
     */
    public static BigDecimal percent(BigDecimal part, BigDecimal whole) {
        return part.divide(whole, MONEY_CONTEXT).multiply(BigDecimal.valueOf(100), MONEY_CONTEXT);
    }

    /**
     * 判断数值是否小于零。
     *
     * @param value 待检查数值
     * @return 为负数时返回 true
     */
    public static boolean isNegative(BigDecimal value) {
        return value != null && value.signum() < 0;
    }

    /**
     * 判断左侧数值是否严格大于右侧数值。
     *
     * @param left 左侧数值
     * @param right 右侧数值
     * @return 左侧大于右侧时返回 true
     */
    public static boolean exceeds(BigDecimal left, BigDecimal right) {
        return left != null && right != null && left.compareTo(right) > 0;
    }
}
