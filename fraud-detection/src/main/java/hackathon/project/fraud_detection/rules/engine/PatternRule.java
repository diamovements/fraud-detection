package hackathon.project.fraud_detection.rules.engine;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.model.Operator;
import hackathon.project.fraud_detection.rules.model.RuleResult;
import hackathon.project.fraud_detection.rules.model.RuleType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Getter
@Setter
public class PatternRule extends Rule {

    private final PatternRuleAnalyzer patternRuleAnalyzer;
    private final ThresholdRule thresholdRule;
    private final UUID id;
    private final int priority;
    private final Boolean enabled;
    private final String by;
    private final Integer windowMin;
    private final String field;
    private final Operator operator;
    private final Object value;
    private final Integer minCount;
    private final RuleType ruleType;

    public PatternRule(UUID id, int priority, boolean enabled, String params, PatternRuleAnalyzer patternRuleAnalyzer) {
        Map<String, Object> parsedParams = parseJsonParams(params);

        this.id = id;
        this.priority = priority;
        this.enabled = enabled;
        this.by = (String) parsedParams.get("by");
        this.windowMin = Integer.valueOf(parsedParams.get("windowMin").toString());
        this.field = (String) parsedParams.get("field");
        this.operator = Operator.valueOf((String) parsedParams.get("operator"));
        this.value = parsedParams.get("value");
        this.minCount = Integer.valueOf(parsedParams.get("minCount").toString());
        this.ruleType = RuleType.PATTERN;
        this.patternRuleAnalyzer = patternRuleAnalyzer;
        this.patternRuleAnalyzer.setPatternRule(this);
        this.thresholdRule = new ThresholdRule(null, priority, enabled, this.field, this.operator, this.value);

        log.info("Created PatternRule: id={}, by={}, field={}, operator={}, value={}, minCount={}, windowMin={}",
                id, by, field, operator, value, minCount, windowMin);
    }

    @Override
    public RuleResult evaluate(TransactionRequest transactionRequest) {
        if (!enabled) {
            log.debug("Pattern rule {} is disabled", id);
            return new RuleResult(false, null);
        }

        // Сначала проверяем базовое условие
        Object fieldValue = transactionRequest.getFieldValue(field);
        log.info("PatternRule {}: Checking condition {} {} {} with value {}",
                id, field, operator, value, fieldValue);

        boolean thresholdTriggered = thresholdRule.evaluateCondition(fieldValue, operator, value);
        log.info("PatternRule {}: Threshold condition triggered: {}", id, thresholdTriggered);

        boolean patternTriggered = false;
        String reason = null;

        if (thresholdTriggered) {
            // Получаем значение для группировки
            String byValue = (by != null && !by.isEmpty()) ?
                    String.valueOf(transactionRequest.getFieldValue(by)) : "default";

            log.info("PatternRule {}: Updating map for by='{}' with value='{}'", id, by, byValue);

            // Обновляем карту транзакций
            patternRuleAnalyzer.updateMap(byValue, transactionRequest.timestamp());

            // Проверяем паттерн
            patternTriggered = patternRuleAnalyzer.checkTransaction(byValue, transactionRequest.timestamp(), minCount);
            log.info("PatternRule {}: Pattern condition triggered: {} (need {} transactions)",
                    id, patternTriggered, minCount);

            if (patternTriggered) {
                reason = String.format("Pattern detected: %s %s %s occurred %d+ times in last %d minutes for %s",
                        field, operator.getSymbol(), value, minCount, windowMin, by);
                log.info("PatternRule {} TRIGGERED: {}", id, reason);
            }
        } else {
            log.debug("PatternRule {}: Threshold condition not met, skipping pattern check", id);
        }

        return new RuleResult(patternTriggered, reason);
    }
}