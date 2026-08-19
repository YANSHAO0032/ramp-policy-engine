package com.ramppolicy.engine.runtime;

import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.OrderRecord;

/**
 * Executes funds actions only for COMPLETE decisions after idempotency gates pass.
 */
public final class ActionExecutor {

    /**
     * Executes the final funds action only when every gate has passed.
     *
     * @param order parsed order
     * @param decision deterministic policy decision
     * @param orderClaimAccepted whether the order-level idempotency claim succeeded
     * @param transactionClaimAccepted whether the transaction-level claim succeeded
     * @return execution result
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
     * Execution outcome for the action gate.
     *
     * @param executed whether a funds action ran
     * @param actionType the executed action type, if any
     */
    public record ActionResult(boolean executed, String actionType) {

        /**
         * Creates a non-executed action result.
         *
         * @return not-executed result
         */
        public static ActionResult notExecuted() {
            return new ActionResult(false, null);
        }
    }
}
