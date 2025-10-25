package hackathon.project.fraud_detection.rules.validator;

import hackathon.project.fraud_detection.rules.model.RuleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class ThresholdJsonParamsChecker extends JsonParamsChecker {

    private static final List<String> NUMERIC_OPERATORS = Arrays.asList(
            "GREATER_THAN", "EQUAL", "NOT_EQUAL", "LESS_THAN",
            "LESS_THAN_OR_EQUAL", "GREATER_THAN_OR_EQUAL"
    );

    private static final List<String> STRING_OPERATORS = Arrays.asList(
            "CONTAINS", "EQUAL", "NOT_EQUAL", "NOT_CONTAINS"
    );

    private static final List<String> TIME_OPERATORS = Arrays.asList(
            "LESS_THAN_OR_EQUAL", "GREATER_THAN_OR_EQUAL"
    );

    @Override
    public boolean checkJsonParams(String params, RuleType ruleType) {
        if (!hasRequiredFields(params)) {
            throw new IllegalArgumentException("Not all neccessary params are present");
        }
        String field = (String) parseJsonParams(params).get("field");
        String operator = (String) parseJsonParams(params).get("operator");

        if (!isValidFieldValue(field)) {
            throw new IllegalArgumentException("Field value is invalid");
        }

        if (!isValidOperator(operator)) {
            throw new IllegalArgumentException("Operator is invalid");
        }

        if (!isValidOperatorForValue(operator, field)) {
            throw new IllegalArgumentException("Value and operator are incompatible");
        }

        return true;
    }

    @Override
    boolean hasRequiredFields(String params) {
        return params.contains("field")
                && params.contains("operator")
                && params.contains("value");
    }

    private boolean isValidFieldValue(String field) {
        return switch (field) {
            case "amount", "transaction_type", "timestamp", "sender_account",
                 "receiver_account", "merchant_category", "device_hash", "location",
                 "payment_channel", "ip_address", "device_used" -> true;
            default -> false;
        };
    }

    private boolean isValidOperator(String operator) {
        return switch (operator) {
            case "EQUAL", "NOT_EQUAL", "LESS_THAN", "GREATER_THAN",
                 "GREATER_THAN_OR_EQUAL", "LESS_THAN_OR_EQUAL",
                 "CONTAINS", "NOT_CONTAINS" -> true;
            default -> false;
        };
    }

    private static Class<?> getFieldType(String field) {
        return switch (field) {
            case "amount" -> BigDecimal.class;
            case "timestamp" -> LocalDateTime.class;
            case "transaction_type", "sender_account", "receiver_account",
                 "merchant_category", "location", "device_used",
                 "payment_channel", "ip_address", "device_hash" -> String.class;
            default -> throw new IllegalArgumentException("Unknown field: " + field);
        };
    }

    private boolean isValidOperatorForValue(String operator, String field) {
        Class<?> fieldType = getFieldType(field);

        if (fieldType == BigDecimal.class) {
            System.out.println(fieldType);
            return NUMERIC_OPERATORS.contains(operator);
        } else if (fieldType == String.class) {
            return STRING_OPERATORS.contains(operator);
        } else if (fieldType == LocalDateTime.class) {
            return TIME_OPERATORS.contains(operator);
        }
        return false;
    }
}
