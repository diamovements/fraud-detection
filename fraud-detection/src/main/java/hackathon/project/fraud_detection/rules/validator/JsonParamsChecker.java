package hackathon.project.fraud_detection.rules.validator;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.model.RuleType;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class JsonParamsChecker {

    protected static final List<String> NUMERIC_OPERATORS = Arrays.asList(
            "GREATER_THAN", "EQUAL", "NOT_EQUAL", "LESS_THAN",
            "LESS_THAN_OR_EQUAL", "GREATER_THAN_OR_EQUAL"
    );

    protected static final List<String> STRING_OPERATORS = Arrays.asList(
            "CONTAINS", "EQUAL", "NOT_EQUAL", "NOT_CONTAINS"
    );

    protected static final List<String> TIME_OPERATORS = Arrays.asList(
            "LESS_THAN_OR_EQUAL", "GREATER_THAN_OR_EQUAL"
    );

    //лишний второй аргумент, класс абстрактный, у нас по классу наследнику понятно какой type
    public abstract boolean checkJsonParams(String params, RuleType ruleType);

     Map<String, Object> parseJsonParams(String params) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(params, new tools.jackson.core.type.TypeReference<>() {});
    }

    protected boolean isValidFieldValue(String field) {
        return switch (field) {
            case "amount", "transaction_type", "timestamp", "sender_account",
                 "receiver_account", "merchant_category", "device_hash", "location",
                 "payment_channel", "ip_address", "device_used" -> true;
            default -> false;
        };
    }

    protected boolean isValidOperator(String operator) {
        return switch (operator) {
            case "EQUAL", "NOT_EQUAL", "LESS_THAN", "GREATER_THAN",
                 "GREATER_THAN_OR_EQUAL", "LESS_THAN_OR_EQUAL",
                 "CONTAINS", "NOT_CONTAINS" -> true;
            default -> false;
        };
    }

    protected boolean isValidOperatorForValue(String operator, String field) {
        Class<?> fieldType = getFieldType(field);

        if (fieldType == BigDecimal.class) {
            return NUMERIC_OPERATORS.contains(operator);
        } else if (fieldType == String.class) {
            return STRING_OPERATORS.contains(operator);
        } else if (fieldType == LocalDateTime.class) {
            return TIME_OPERATORS.contains(operator);
        }
        return false;
    }

    protected boolean isValidValueForOperator(String value, String operator) {
        if (NUMERIC_OPERATORS.contains(operator)) {
           try{
               new BigDecimal(value);
               return true;
           } catch (Exception exp){
               return false;
           }
        };
        if (TIME_OPERATORS.contains(operator)) {
            try{
                LocalDateTime.parse(value);
                return true;
            } catch (Exception exp){
                return false;
            }
        };
        if (STRING_OPERATORS.contains(operator)) {
            return true;
        };
        return false;
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

    protected boolean isInteger(String str) {
        try{
            Integer.parseInt(str);
            return true;
        }
        catch (Exception exp){
            return false;
        }
    }
}
