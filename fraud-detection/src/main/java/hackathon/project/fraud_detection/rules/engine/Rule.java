package hackathon.project.fraud_detection.rules.engine;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.model.RuleResult;
import hackathon.project.fraud_detection.rules.model.RuleType;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Getter
public abstract class Rule {
    UUID id;
    RuleType ruleType;
    int priority;
    String name;
    abstract public RuleResult evaluate(TransactionRequest transactionRequest);
}
