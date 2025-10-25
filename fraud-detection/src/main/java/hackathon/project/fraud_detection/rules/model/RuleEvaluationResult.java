package hackathon.project.fraud_detection.rules.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
// класс, который содержит в себе итоговую метку для транзакции, список стриггернувших правил и результаты правил
public class RuleEvaluationResult {
    private final boolean suspicious;
    private final List<String> triggeredRuleNames;
    private final List<RuleResult> ruleResults;
    private final List<String> reason;
}
