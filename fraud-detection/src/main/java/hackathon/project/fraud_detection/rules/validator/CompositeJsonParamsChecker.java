package hackathon.project.fraud_detection.rules.validator;

import hackathon.project.fraud_detection.rules.engine.dsl.ExpressionParser;
import hackathon.project.fraud_detection.rules.model.RuleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class CompositeJsonParamsChecker extends JsonParamsChecker {
    @Override
    public boolean checkJsonParams(String params, RuleType ruleType) {
        return hasRequiredFields(params) && hasCorrectSyntax(params);
    }

    boolean hasRequiredFields(String params) {
        return params.contains("expr");
    }

    public boolean hasCorrectSyntax(String params) {
        try {
            ExpressionParser parser = new ExpressionParser((String) parseJsonParams(params).get("expr"));
            parser.parse();
        } catch (Exception e) {
            throw new IllegalArgumentException("Syntax in rule is incorrect");
        }
        return true;
    }
}
