package com.ramppolicy.engine.plan;

import com.ramppolicy.engine.domain.OrderType;

/**
 * 根据订单类型解析显式规则计划。
 */
public interface RulePlanResolver {

    /**
     * 解析某一订单类型对应的适用规则计划。
     *
     * @param orderType 订单类型
     * @return 适用规则计划
     */
    RulePlan resolve(OrderType orderType);
}
