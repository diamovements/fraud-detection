package hackathon.project.fraud_detection.rules.engine;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.model.RuleResult;

// класс для паттерн правила, не реализован
public class PatternRule extends Rule {

    @Override
    public RuleResult evaluate(TransactionRequest transactionRequest, RuleContext ruleContext) {
        return null;
    }
}
