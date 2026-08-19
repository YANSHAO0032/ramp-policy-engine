package com.ramppolicy.engine.eval;

import com.ramppolicy.engine.domain.Decision;
import com.ramppolicy.engine.domain.EscalationTarget;
import com.ramppolicy.engine.domain.ReasonCode;
import com.ramppolicy.engine.domain.Retryability;
import com.ramppolicy.engine.runtime.OrderExecutionRecord;

import java.util.Comparator;
import java.util.List;

/**
 * 只包含资金授权路径所需的确定性指纹。
 */
public record DecisionFingerprint(
        String orderId,
        Decision decision,
        List<String> reasonCodes,
        List<String> escalationTargets,
        Retryability retryability,
        boolean actionAuthorized) {

    public static DecisionFingerprint from(OrderExecutionRecord record) {
        return new DecisionFingerprint(
                record.orderId(),
                record.decision(),
                record.reasonCodes().stream().map(ReasonCode::name).sorted().toList(),
                record.escalationTargets().stream().map(EscalationTarget::name).sorted().toList(),
                record.retryability(),
                record.actionExecuted());
    }

    public static Comparator<DecisionFingerprint> comparator() {
        return Comparator.comparing(DecisionFingerprint::orderId)
                .thenComparing(fingerprint -> fingerprint.decision().name())
                .thenComparing(fingerprint -> String.join(",", fingerprint.reasonCodes()))
                .thenComparing(fingerprint -> String.join(",", fingerprint.escalationTargets()))
                .thenComparing(fingerprint -> fingerprint.retryability().name())
                .thenComparing(DecisionFingerprint::actionAuthorized);
    }
}
