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

    public ThresholdRule(UUID id, int priority, boolean enabled, String params) {
        this.id = id;
        this.priority = priority;
        this.enabled = enabled;
        this.field = (String) parseJsonParams(params).get("field");
        this.operator = Operator.valueOf((String) parseJsonParams(params).get("operator"));
        this.value = parseJsonParams(params).get("value");
        this.ruleType = RuleType.THRESHOLD;
    }

    @Override
    public RuleResult evaluate(TransactionRequest transactionRequest, RuleContext ruleContext) {
        try {
            Object fieldValue = getFieldValue(transactionRequest, field);
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

    private Object getFieldValue(TransactionRequest transaction, String field) {
        return switch (field) {
            case "amount" -> transaction.amount();
            case "transaction_type" -> transaction.transactionType();
            case "timestamp" -> transaction.timestamp();
            case "sender_account" -> transaction.senderAccount();
            case "receiver_account" -> transaction.receiverAccount();
            case "merchant_category" -> transaction.merchantCategory();
            case "location" -> transaction.location();
            case "device_used" -> transaction.deviceUsed();
            case "payment_channel" -> transaction.paymentChannel();
            case "ip_address" -> transaction.ipAddress();
            case "device_hash" -> transaction.deviceHash();
            default -> throw new IllegalArgumentException("Unknown field: " + field);
        };
    }

    private boolean evaluateCondition(Object actual, Operator operator, Object expected) {
        if (actual instanceof LocalDateTime && expected instanceof String) {
            expected = LocalTime.parse((String) expected);
        } if (actual instanceof BigDecimal && expected instanceof String) {
            expected = BigDecimal.valueOf(Double.parseDouble((String) expected));
        }

        return switch (actual) {
            case BigDecimal bigDecimal -> evaluateNumericCondition(bigDecimal,
                    operator, (BigDecimal) expected);
            case String s -> evaluateStringCondition(s, operator, (String) expected);
            case LocalDateTime localDateTime -> evaluateTimeCondition(localDateTime.toLocalTime(),
                    operator, expected);
            default -> throw new IllegalArgumentException("Unsupported type of value: " + actual.getClass());
        };
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

    private Map<String, Object> parseJsonParams(String params) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(params, new tools.jackson.core.type.TypeReference<>() {});
    }
}
