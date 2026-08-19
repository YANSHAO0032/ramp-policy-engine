package com.ramppolicy.engine.plan;

import com.ramppolicy.engine.domain.OrderType;
import com.ramppolicy.engine.domain.PolicyVersion;

import java.util.EnumSet;
import java.util.List;

/**
 * V4 Demo 策略规则矩阵的静态解析器。
 */
public final class StaticRulePlanResolver implements RulePlanResolver {

    @Override
    public RulePlan resolve(OrderType orderType) {
        return switch (orderType) {
            case ON_RAMP -> onRamp();
            case OFF_RAMP -> offRamp();
            case WITHDRAWAL -> withdrawal();
        };
    }

    private static RulePlan onRamp() {
        return new RulePlan(
                OrderType.ON_RAMP,
                PolicyVersion.VALUE,
                List.of(
                        mandatory(RuleId.CUSTOMER_STATUS),
                        mandatory(RuleId.ASSET_SUPPORT),
                        mandatory(RuleId.ADDRESS_RISK),
                        mandatory(RuleId.KYC_LIMIT),
                        mandatory(RuleId.MINIMUM_AMOUNT),
                        mandatory(RuleId.FIAT_RECEIPT),
                        mandatory(RuleId.QUOTE_EXPIRY),
                        mandatory(RuleId.ON_RAMP_CONSERVATION),
                        mandatory(RuleId.TRAVEL_RULE),
                        advisory(RuleId.VASP_UNKNOWN_WARNING)),
                EnumSet.of(
                        FactRequirement.CUSTOMER,
                        FactRequirement.ASSET_CONFIG,
                        FactRequirement.COUNTERPARTY_ADDRESS_RISK,
                        FactRequirement.FIAT_RECEIPT,
                        FactRequirement.QUOTE,
                        FactRequirement.REFERENCE_RATE,
                        FactRequirement.COUNTERPARTY_VASP));
    }

    private static RulePlan offRamp() {
        return new RulePlan(
                OrderType.OFF_RAMP,
                PolicyVersion.VALUE,
                List.of(
                        mandatory(RuleId.CUSTOMER_STATUS),
                        mandatory(RuleId.ASSET_SUPPORT),
                        mandatory(RuleId.ADDRESS_RISK),
                        mandatory(RuleId.KYC_LIMIT),
                        mandatory(RuleId.MINIMUM_AMOUNT),
                        mandatory(RuleId.CONFIRMATION),
                        mandatory(RuleId.AMOUNT_MATCH),
                        mandatory(RuleId.PAYOUT_CONSERVATION),
                        mandatory(RuleId.NETWORK_MATCH),
                        mandatory(RuleId.BANK_OWNERSHIP),
                        mandatory(RuleId.QUOTE_EXPIRY),
                        mandatory(RuleId.TRAVEL_RULE),
                        advisory(RuleId.VASP_UNKNOWN_WARNING)),
                EnumSet.of(
                        FactRequirement.CUSTOMER,
                        FactRequirement.ASSET_CONFIG,
                        FactRequirement.COUNTERPARTY_ADDRESS_RISK,
                        FactRequirement.QUOTE,
                        FactRequirement.REFERENCE_RATE,
                        FactRequirement.DEPOSIT,
                        FactRequirement.PAYOUT,
                        FactRequirement.BANK_IDENTITY,
                        FactRequirement.COUNTERPARTY_VASP));
    }

    private static RulePlan withdrawal() {
        return new RulePlan(
                OrderType.WITHDRAWAL,
                PolicyVersion.VALUE,
                List.of(
                        mandatory(RuleId.CUSTOMER_STATUS),
                        mandatory(RuleId.ASSET_SUPPORT),
                        mandatory(RuleId.ADDRESS_RISK),
                        mandatory(RuleId.KYC_LIMIT),
                        mandatory(RuleId.MINIMUM_AMOUNT),
                        mandatory(RuleId.WITHDRAWAL_FUNDS),
                        mandatory(RuleId.TRAVEL_RULE),
                        advisory(RuleId.VASP_UNKNOWN_WARNING)),
                EnumSet.of(
                        FactRequirement.CUSTOMER,
                        FactRequirement.ASSET_CONFIG,
                        FactRequirement.COUNTERPARTY_ADDRESS_RISK,
                        FactRequirement.REFERENCE_RATE,
                        FactRequirement.WALLET_FUNDS,
                        FactRequirement.COUNTERPARTY_VASP));
    }

    private static PlannedRule mandatory(RuleId ruleId) {
        return new PlannedRule(ruleId, true);
    }

    private static PlannedRule advisory(RuleId ruleId) {
        return new PlannedRule(ruleId, false);
    }
}
