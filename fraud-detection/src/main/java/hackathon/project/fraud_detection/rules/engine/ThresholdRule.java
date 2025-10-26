package hackathon.project.fraud_detection.rules.engine;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.model.Operator;
import hackathon.project.fraud_detection.rules.model.RuleResult;
import hackathon.project.fraud_detection.rules.model.RuleType;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class ThresholdRule extends Rule {

    private final UUID id;
    private final int priority;
    private final boolean enabled;
    private final String field;
    private final Operator operator;
    private final Object value;
    private final RuleType ruleType;
    private final String name;

    public ThresholdRule(UUID id, int priority, boolean enabled, String params, String name) {
        this.id = id;
        this.priority = priority;
        this.enabled = enabled;
        this.field = (String) parseJsonParams(params).get("field");
        this.operator = Operator.valueOf((String) parseJsonParams(params).get("operator"));
        this.value = parseJsonParams(params).get("value");
        this.name = name;
        this.ruleType = RuleType.THRESHOLD;
    }

    public ThresholdRule(UUID id, int priority, boolean enabled, String field, Operator operator, Object value) {
        this.id = id;
        this.priority = priority;
        this.enabled = enabled;
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.ruleType = RuleType.THRESHOLD;
    }

    @Override
    public RuleResult evaluate(TransactionRequest transactionRequest) {
        try {
            Object fieldValue = transactionRequest.getFieldValue(field);
            log.info("fieldValue, operator, value: {}, {}, {}", fieldValue, operator, value);
            boolean triggered = evaluateCondition(fieldValue, operator, value);
            String reason = triggered ?
                    String.format("%s %s %s", field, operator.getSymbol(), value) : null;

            if (reason != null) {
                log.info("Reason for triggered rule {}: {}", id, reason);
            }
            return new RuleResult(triggered, reason);

        } catch (Exception e) {
            log.error("Error evaluating threshold rule: {}", id, e);
            return new RuleResult(false, "Evaluation of threshold rule failed: " + e.getMessage());
        }
    }

    //подумать как выпилить отсюда проверки тк все теперь валидируется на входе
    // и некорректные params в базу не попадают

    public boolean evaluateCondition(Object actual, Operator operator, Object expected) {
        if (actual instanceof LocalDateTime) {
            expected = LocalTime.parse((String) expected);
            LocalTime localTime = LocalTime.parse((String) actual);
            return evaluateTimeCondition(localTime,
                    operator, expected);
        } else if (actual instanceof BigDecimal || actual instanceof Integer) {
            expected = new BigDecimal((String) expected);
            BigDecimal bigDecimal = BigDecimal.valueOf(Double.parseDouble((String) actual));
            return evaluateNumericCondition(bigDecimal,
                    operator, (BigDecimal) expected);
        }
        else{
            evaluateStringCondition((String) actual, operator, (String) expected);
        }
        throw new IllegalArgumentException("Unsupported type of value: " + actual.getClass());
    }


    private boolean evaluateNumericCondition(BigDecimal actual, Operator operator, BigDecimal expected) {
        return switch (operator) {
            case GREATER_THAN -> actual.compareTo(expected) > 0;
            case GREATER_THAN_OR_EQUAL -> actual.compareTo(expected) >= 0;
            case LESS_THAN -> actual.compareTo(expected) < 0;
            case LESS_THAN_OR_EQUAL -> actual.compareTo(expected) <= 0;
            case EQUAL -> actual.compareTo(expected) == 0;
            case NOT_EQUAL -> actual.compareTo(expected) != 0;
            default -> throw new IllegalArgumentException("Illegal operator used for numeric condition: " + operator);
        };
    }

    private boolean evaluateStringCondition(String actual, Operator operator, String expected) {
        return switch (operator) {
            case CONTAINS -> actual.contains(expected);
            case NOT_CONTAINS -> !actual.contains(expected);
            case EQUAL -> actual.equals(expected);
            case NOT_EQUAL -> !actual.equals(expected);
            default -> throw new IllegalArgumentException("Illegal operator used for string condition: " + operator);
        };
    }

    private boolean evaluateTimeCondition(LocalTime actual, Operator operator, Object expected) {
        if (expected instanceof String) {
            expected = LocalTime.parse((String) expected);
        }
        LocalTime expectedTime = parseTime(expected.toString());

        return switch (operator) {
            case LESS_THAN_OR_EQUAL -> !actual.isAfter(expectedTime);
            case GREATER_THAN_OR_EQUAL -> !actual.isBefore(expectedTime);
            default -> throw new IllegalArgumentException("Illegal operator used for time condition: " + operator);
        };
    }

    private LocalTime parseTime(String time) {
        return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
    }

}
