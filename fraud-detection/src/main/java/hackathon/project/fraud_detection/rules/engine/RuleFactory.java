package hackathon.project.fraud_detection.rules.engine;

import hackathon.project.fraud_detection.rules.ml.MLClient;
import hackathon.project.fraud_detection.storage.entity.RuleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RuleFactory {

    private final PatternRuleAnalyzerStorage patternRuleAnalyzerStorage;
    private final MLClient mlClient;

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
            case ML:
                return new MLRule(
                        ruleEntity.getId(),
                        ruleEntity.getPriority(),
                        ruleEntity.isEnabled(),
                        ruleEntity.getParams(),
                        ruleEntity.getName(),
                        mlClient
                );
            case PATTERN:
                PatternRuleAnalyzer analyzer = new PatternRuleAnalyzer();
                PatternRule patternRule = new PatternRule(
                        ruleEntity.getId(),
                        ruleEntity.getPriority(),
                        ruleEntity.isEnabled(),
                        ruleEntity.getParams(),
                        analyzer
                );
                patternRuleAnalyzerStorage.addNewPatternRule(analyzer);
                return patternRule;
            default:
                throw new IllegalArgumentException("Unsupported rule type: " + ruleEntity.getType());
        }
    }
}