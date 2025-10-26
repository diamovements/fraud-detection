package hackathon.project.fraud_detection.rules.engine;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.ml.MLClient;
import hackathon.project.fraud_detection.rules.model.Operator;
import hackathon.project.fraud_detection.rules.model.RuleResult;
import hackathon.project.fraud_detection.rules.model.RuleType;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

@Slf4j
public class MLRule extends Rule {

    private final UUID id;
    private final int priority;
    private final boolean enabled;
    private final String modelName;
    private final Operator operator;
    private final Object value;
    private final RuleType ruleType;
    private final String name;
    private final MLClient mlClient;

    public MLRule(UUID id, int priority, boolean enabled, String params, String name, MLClient mlClient) {
        this.id = id;
        this.priority = priority;
        this.enabled = enabled;
        this.modelName = (String) parseJsonParams(params).get("model_name");
        this.operator = Operator.valueOf((String) parseJsonParams(params).get("operator"));
        this.value = parseJsonParams(params).get("value");
        this.mlClient = mlClient;
        this.ruleType = RuleType.ML;
        this.name = name;
    }

    @Override
    public RuleResult evaluate(TransactionRequest transactionRequest) {
        double fraudProbability = mlClient.predictFraud(
                modelName,
                transactionRequest
        ).fraudProbability();
        log.info("model_name, operator, value: {}, {}, {}", modelName, operator, value);
        double typedValue = castToDouble(value);

        boolean triggered = evaluateCondition(fraudProbability, operator, typedValue);

        String reason = triggered ?
                String.format("ML модель '%s' предсказала мошенничество с вероятностью %.4f %s %.4f",
                        modelName, fraudProbability, operator, typedValue) :
                String.format("ML модель '%s': вероятность мошенничества %.4f %s %.4f",
                        modelName, fraudProbability, operator, typedValue);

        return new RuleResult(triggered, reason);
    }

    private boolean evaluateCondition(double actual, Operator operator, double expected) {
        return switch (operator) {
            case GREATER_THAN -> actual > expected;
            case GREATER_THAN_OR_EQUAL -> actual >= expected;
            case EQUAL -> actual == expected;
            case NOT_EQUAL -> actual != expected;
            default -> throw new IllegalArgumentException("Illegal operator used for numeric condition: " + operator);
        };
    }

    private double castToDouble(Object value) {
        if (value instanceof String) {
            return Double.parseDouble((String) value);
        } else {
            throw new IllegalArgumentException("Illegal type used for numeric condition: " + value);
        }
    }
}
