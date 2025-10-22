package hackathon.project.fraud_detection.rules.engine;

import hackathon.project.fraud_detection.rules.engine.dsl.DslParser;
import hackathon.project.fraud_detection.rules.model.RuleType;
import hackathon.project.fraud_detection.storage.entity.RuleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
// отдельная фабрика тк юзаем внутри другую фабрику для проверки срабатывания
// подправил, из-за этого была циклическая зависимость с сервисом SubRuleEvaluationService
public class CompositeRuleFactory {

    private final DslParser dslParser;
    private final SubRuleEvaluationService subRuleEvaluationService;

    public Rule createCompositeRule(RuleEntity ruleEntity) {
        if (ruleEntity.getType().equals(RuleType.COMPOSITE)) {
            return new CompositeRule(
                    ruleEntity.getId(),
                    ruleEntity.getPriority(),
                    ruleEntity.isEnabled(),
                    ruleEntity.getName(),
                    ruleEntity.getParams(),
                    subRuleEvaluationService,
                    dslParser
            );
        } else {
            throw new IllegalArgumentException("Unsupported rule type: "+ ruleEntity.getType());
        }
    }
}
