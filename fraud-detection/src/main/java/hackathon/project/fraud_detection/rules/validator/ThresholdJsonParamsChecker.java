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
    @Override
    public boolean checkJsonParams(String params, RuleType ruleType) {
        String field = (String) parseJsonParams(params).get("field");
        String operator = (String) parseJsonParams(params).get("operator");
        if (field == null || operator == null){
            throw new IllegalArgumentException("Not all neccessary params are present");
        }

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

}
