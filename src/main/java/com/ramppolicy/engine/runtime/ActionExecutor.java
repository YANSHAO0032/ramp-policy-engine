package com.ramppolicy.engine.runtime;

import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.OrderRecord;

/**
 * 仅在 COMPLETE 决策且幂等门通过后执行资金动作。
 */
public final class ActionExecutor {

    /**
     * 只有所有执行门都通过时才执行最终资金动作。
     *
     * @param order 已解析订单
     * @param decision 确定性策略决策
     * @param orderClaimAccepted 订单级幂等占用是否成功
     * @param transactionClaimAccepted 交易级幂等占用是否成功
     * @return 执行动作结果
     */
    public ActionResult execute(OrderRecord order, Decision decision, boolean orderClaimAccepted, boolean transactionClaimAccepted) {
        if (decision != Decision.COMPLETE) {
            return ActionResult.notExecuted();
        }
        if (!orderClaimAccepted || !transactionClaimAccepted) {
            return ActionResult.notExecuted();
        }
        String actionType = switch (order.type()) {
            case ON_RAMP -> "send_crypto";
            case OFF_RAMP -> "send_fiat";
            case WITHDRAWAL -> "send_crypto";
        };
        return new ActionResult(true, actionType);
    }

    /**
     * 执行门之后的动作结果。
     *
     * @param executed 是否执行了资金动作
     * @param actionType 已执行的动作类型，未执行时为空
     */
    public record ActionResult(boolean executed, String actionType) {

        /**
         * 创建未执行资金动作的结果。
         *
         * @return 未执行结果
         */
        public static ActionResult notExecuted() {
            return new ActionResult(false, null);
        }
    }
}
