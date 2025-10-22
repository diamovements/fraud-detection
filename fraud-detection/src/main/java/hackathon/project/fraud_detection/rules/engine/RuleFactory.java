package hackathon.project.fraud_detection.rules.engine;

import hackathon.project.fraud_detection.storage.entity.RuleEntity;
import org.springframework.stereotype.Component;

@Component
public class RuleFactory {

    public Rule createRule(RuleEntity ruleEntity) {
        switch (ruleEntity.getType()) {
            case THRESHOLD:
                return new ThresholdRule(
                        ruleEntity.getId(),
                        ruleEntity.getPriority(),
                        ruleEntity.isEnabled(),
                        ruleEntity.getParams(),
                        ruleEntity.getName()
                );
            case PATTERN:
                return new PatternRule();
            default:
                throw new IllegalArgumentException("Unsupported rule type: " + ruleEntity.getType());
        }
    }
}
