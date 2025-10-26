package hackathon.project.fraud_detection.rules.validator;

import hackathon.project.fraud_detection.rules.engine.Rule;
import hackathon.project.fraud_detection.rules.model.RuleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PatternJsonParamsChecker extends JsonParamsChecker {

    @Override
    public boolean checkJsonParams(String params, RuleType ruleType) {

        String by = (String) parseJsonParams(params).get("by");
        String windowMin = parseJsonParams(params).get("windowMin").toString();
        String operator = (String) parseJsonParams(params).get("operator");
        String field = (String) parseJsonParams(params).get("field");
        String value = parseJsonParams(params).get("value").toString();
        String minCount = parseJsonParams(params).get("minCount").toString();

        if (by == null || windowMin == null || operator == null || field == null || value == null || minCount == null) {
            throw new IllegalArgumentException("Not all neccessary params are present");
        }

        if (!isValidOperator(operator)) {
            throw new IllegalArgumentException("Operator is invalid");
        }

        if (!isValidFieldValue(field)) {
            throw new IllegalArgumentException("Field value is invalid");
        }

        if (!isValidOperatorForValue(operator, field)) {
            throw new IllegalArgumentException("Field and operator are incompatible");
        }

        if (!isValidFieldValue(by)) {
            throw new IllegalArgumentException("By value is invalid");
        }

        if (!isInteger(windowMin)) {
            throw new IllegalArgumentException("WindowMin is invalid");
        }

        if (!isInteger(minCount)) {
            throw new IllegalArgumentException("MinCount is invalid");
        }
        return true;
    }

}
