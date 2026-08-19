package com.ramppolicy.engine.plan;

import com.ramppolicy.engine.domain.OrderType;
import com.ramppolicy.engine.domain.PolicyVersion;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RulePlanResolverTest {

    private final RulePlanResolver resolver = new StaticRulePlanResolver();

    @Test
    void onRampPlanExcludesOffRampAndWithdrawalOnlyRules() {
        RulePlan plan = resolver.resolve(OrderType.ON_RAMP);

        assertEquals(PolicyVersion.VALUE, plan.policyVersion());
        assertTrue(hasMandatory(plan, RuleId.CUSTOMER_STATUS));
        assertTrue(hasMandatory(plan, RuleId.FIAT_RECEIPT));
        assertTrue(hasMandatory(plan, RuleId.ON_RAMP_CONSERVATION));
        assertFalse(hasRule(plan, RuleId.CONFIRMATION));
        assertFalse(hasRule(plan, RuleId.AMOUNT_MATCH));
        assertFalse(hasRule(plan, RuleId.PAYOUT_CONSERVATION));
        assertFalse(hasRule(plan, RuleId.NETWORK_MATCH));
        assertFalse(hasRule(plan, RuleId.BANK_OWNERSHIP));
        assertFalse(hasRule(plan, RuleId.WITHDRAWAL_FUNDS));
    }

    @Test
    void offRampPlanExcludesOnRampAndWithdrawalOnlyRules() {
        RulePlan plan = resolver.resolve(OrderType.OFF_RAMP);

        assertTrue(hasMandatory(plan, RuleId.CONFIRMATION));
        assertTrue(hasMandatory(plan, RuleId.AMOUNT_MATCH));
        assertTrue(hasMandatory(plan, RuleId.PAYOUT_CONSERVATION));
        assertTrue(hasMandatory(plan, RuleId.NETWORK_MATCH));
        assertTrue(hasMandatory(plan, RuleId.BANK_OWNERSHIP));
        assertFalse(hasRule(plan, RuleId.FIAT_RECEIPT));
        assertFalse(hasRule(plan, RuleId.ON_RAMP_CONSERVATION));
        assertFalse(hasRule(plan, RuleId.WITHDRAWAL_FUNDS));
    }

    @Test
    void withdrawalPlanExcludesFiatQuoteAndOffRampOnlyRules() {
        RulePlan plan = resolver.resolve(OrderType.WITHDRAWAL);

        assertTrue(hasMandatory(plan, RuleId.WITHDRAWAL_FUNDS));
        assertFalse(hasRule(plan, RuleId.FIAT_RECEIPT));
        assertFalse(hasRule(plan, RuleId.QUOTE_EXPIRY));
        assertFalse(hasRule(plan, RuleId.CONFIRMATION));
        assertFalse(hasRule(plan, RuleId.AMOUNT_MATCH));
        assertFalse(hasRule(plan, RuleId.PAYOUT_CONSERVATION));
        assertFalse(hasRule(plan, RuleId.NETWORK_MATCH));
        assertFalse(hasRule(plan, RuleId.BANK_OWNERSHIP));
    }

    @Test
    void travelRuleIsMandatoryAndVaspUnknownWarningIsAdvisoryForEveryOrderType() {
        for (OrderType orderType : OrderType.values()) {
            RulePlan plan = resolver.resolve(orderType);

            assertTrue(hasMandatory(plan, RuleId.TRAVEL_RULE));
            assertTrue(hasAdvisory(plan, RuleId.VASP_UNKNOWN_WARNING));
        }
    }

    @Test
    void requiredFactsFollowOrderDirection() {
        assertTrue(resolver.resolve(OrderType.ON_RAMP).requiredFacts().contains(FactRequirement.FIAT_RECEIPT));
        assertTrue(resolver.resolve(OrderType.OFF_RAMP).requiredFacts().contains(FactRequirement.DEPOSIT));
        assertTrue(resolver.resolve(OrderType.OFF_RAMP).requiredFacts().contains(FactRequirement.PAYOUT));
        assertTrue(resolver.resolve(OrderType.WITHDRAWAL).requiredFacts().contains(FactRequirement.WALLET_FUNDS));
        assertFalse(resolver.resolve(OrderType.ON_RAMP).requiredFacts().contains(FactRequirement.DEPOSIT));
    }

    private static boolean hasRule(RulePlan plan, RuleId ruleId) {
        return plannedRules(plan).containsKey(ruleId);
    }

    private static boolean hasMandatory(RulePlan plan, RuleId ruleId) {
        return Boolean.TRUE.equals(plannedRules(plan).get(ruleId));
    }

    private static boolean hasAdvisory(RulePlan plan, RuleId ruleId) {
        return Boolean.FALSE.equals(plannedRules(plan).get(ruleId));
    }

    private static Map<RuleId, Boolean> plannedRules(RulePlan plan) {
        return plan.rules().stream()
                .collect(Collectors.toMap(PlannedRule::ruleId, PlannedRule::mandatory));
    }
}
