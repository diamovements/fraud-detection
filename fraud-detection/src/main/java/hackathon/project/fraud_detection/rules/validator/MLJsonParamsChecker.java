package hackathon.project.fraud_detection.rules.validator;

import hackathon.project.fraud_detection.rules.model.RuleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@Slf4j
public class MLJsonParamsChecker extends JsonParamsChecker {
    @Override
    public boolean checkJsonParams(String params, RuleType ruleType) {
        if (!hasRequiredFields(params)) {
            throw new IllegalArgumentException("Not all necessary params are present");
        }
        String operator = (String) parseJsonParams(params).get("operator");
        String modelName = (String) parseJsonParams(params).get("model_name");
        Object value = parseJsonParams(params).get("value");

        if (!isValidOperator(operator)) {
            throw new IllegalArgumentException("Operator is invalid");
        }

        if (!isValidModelName(modelName)) {
            throw new IllegalArgumentException("Model name should not contain extension");
        }

        if (!isValidTypeOfValue(value)) {
            throw new IllegalArgumentException("Value type should be double");
        }

        return true;
    }

    @Override
    boolean hasRequiredFields(String params) {
        return params.contains("model_name")
                && params.contains("operator")
                && params.contains("value");
    }

    private boolean isValidOperator(String operator) {
        return switch (operator) {
            case "EQUAL",
                 "NOT_EQUAL",
                 "GREATER_THAN",
                 "GREATER_THAN_OR_EQUAL" -> true;
            default -> false;
        };
    }

    private boolean isValidModelName(String modelName) {
        return !modelName.contains(".");
    }

    private boolean isValidTypeOfValue(Object value) {
        if (value instanceof String) {
            try {
                Double.parseDouble(value.toString());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } else return value instanceof Number;
    }
}
