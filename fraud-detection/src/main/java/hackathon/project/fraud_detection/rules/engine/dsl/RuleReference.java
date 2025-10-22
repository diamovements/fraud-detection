package hackathon.project.fraud_detection.rules.engine.dsl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
// ссылка на правило по имени
public class RuleReference extends Expression {
    private final String name;

    @Override
    public boolean evaluate(EvaluationContext context) {
        return context.evaluateSingleRule(name).triggered();
    }
}
