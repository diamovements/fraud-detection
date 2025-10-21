package hackathon.project.fraud_detection.api.dto.request;

import hackathon.project.fraud_detection.rules.model.RuleType;

public record CreateRuleRequest(
        RuleType type,
        boolean enabled,
        int priority,
        String params,
        int version
) {
}
