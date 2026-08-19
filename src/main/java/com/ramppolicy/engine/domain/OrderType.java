package com.ramppolicy.engine.domain;

/**
 * Demo 策略引擎支持的订单资金方向。
 */
public enum OrderType {
    /**
     * 客户支付法币，平台向客户外部地址发送加密资产。
     */
    ON_RAMP,

    /**
     * 客户向平台入金加密资产，平台向客户银行账户支付法币。
     */
    OFF_RAMP,

    /**
     * 客户从平台账户向外部地址提取加密资产。
     */
    WITHDRAWAL
}
