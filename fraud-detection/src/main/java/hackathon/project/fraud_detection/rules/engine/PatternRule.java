package hackathon.project.fraud_detection.rules.engine;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.model.Operator;
import hackathon.project.fraud_detection.rules.model.RuleResult;
import hackathon.project.fraud_detection.rules.model.RuleType;
import jdk.jfr.Enabled;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private final Object windowMin;
    private final String field;
    private final Operator operator;
    private final Object value;
    private final Object minCount;
    private final RuleType ruleType;

    public PatternRule(UUID id, int priority, boolean enabled, String params, PatternRuleAnalyzer patternRuleAnalyzer) {
        this.id = id;
        this.priority = priority;
        this.enabled = enabled;
        this.by = (String) parseJsonParams(params).get("by");
        this.windowMin = parseJsonParams(params).get("windowMin");
        this.field = (String) parseJsonParams(params).get("field");
        this.operator = Operator.valueOf((String) parseJsonParams(params).get("operator"));
        this.value = parseJsonParams(params).get("value");
        this.minCount = parseJsonParams(params).get("minCount");
        this.ruleType = RuleType.PATTERN;
        this.patternRuleAnalyzer = patternRuleAnalyzer;
        this.patternRuleAnalyzer.setPatternRule(this);
        this.thresholdRule = new ThresholdRule(null, priority, enabled, this.field, this.operator, this.value);
    }

    @Override
    public RuleResult evaluate(TransactionRequest transactionRequest) {
        boolean triggered = thresholdRule.evaluateCondition(transactionRequest.getFieldValue(field), operator, value);
        if (triggered) {
            triggered = patternRuleAnalyzer.checkTransaction(by, transactionRequest.timestamp(), (Integer) minCount);
        }
        else {
            triggered = false;
        }
        String reason = triggered ?
                String.format("%s %s %s", field, operator.getSymbol(), value) : null;
        RuleResult ruleResult = new RuleResult(triggered, reason);
        return ruleResult;
    }

}
