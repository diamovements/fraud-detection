package hackathon.project.fraud_detection.rules.engine;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.model.RuleResult;
import hackathon.project.fraud_detection.storage.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubRuleEvaluationService {

    private final RuleRepository ruleRepository;
    private final RuleFactory ruleFactory;

    public RuleResult evaluateSingleRule(String name, TransactionRequest transactionRequest) {
        var ruleFromDb = ruleRepository.findRuleEntityByName(name);
        log.info("Evaluating subrule with name: {}", name);
        var rule = ruleFactory.createRule(ruleFromDb);
        return rule.evaluate(transactionRequest);
    }
}
