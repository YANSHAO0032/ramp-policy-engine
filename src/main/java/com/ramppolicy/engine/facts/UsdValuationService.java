package com.ramppolicy.engine.facts;

import com.ramppolicy.engine.domain.BigDecimals;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 使用参考汇率将 Demo 中的加密资产和法币金额折算为 USD。
 */
public final class UsdValuationService {

    private final Map<String, BigDecimal> referenceRates;

    public UsdValuationService(Map<String, BigDecimal> referenceRates) {
        this.referenceRates = Map.copyOf(referenceRates);
    }

    /**
     * 使用参考汇率表将加密资产数量折算为 USD。
     *
     * @param asset 资产代码
     * @param amount 加密资产数量
     * @return USD 估值；汇率缺失时返回 null
     */
    public BigDecimal cryptoToUsd(String asset, BigDecimal amount) {
        BigDecimal rate = referenceRates.get(asset + "/USD");
        if (rate == null || amount == null) {
            return null;
        }
        return amount.multiply(rate, BigDecimals.MONEY_CONTEXT);
    }

    /**
     * 使用参考汇率表将法币金额折算为 USD。
     *
     * @param currency 法币币种
     * @param amount 法币金额
     * @return USD 估值；汇率缺失时返回 null
     */
    public BigDecimal fiatToUsd(String currency, BigDecimal amount) {
        if ("USD".equals(currency)) {
            return amount;
        }
        BigDecimal rate = referenceRates.get(currency + "/USD");
        if (rate == null || amount == null) {
            return null;
        }
        return amount.multiply(rate, BigDecimals.MONEY_CONTEXT);
    }
}
