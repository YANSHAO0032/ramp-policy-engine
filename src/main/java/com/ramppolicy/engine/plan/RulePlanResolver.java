package com.ramppolicy.engine.plan;

import com.ramppolicy.engine.domain.OrderType;

/**
 * Resolves the explicit rule plan for an order type.
 */
public interface RulePlanResolver {

    /**
     * Resolves the applicable plan for one order type.
     *
     * @param orderType order type
     * @return applicable rule plan
     */
    RulePlan resolve(OrderType orderType);
}
