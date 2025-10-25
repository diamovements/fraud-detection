package hackathon.project.fraud_detection.rules.service;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.engine.CompositeRuleFactory;
import hackathon.project.fraud_detection.rules.engine.RuleFactory;
import hackathon.project.fraud_detection.rules.model.RuleEvaluationResult;
import hackathon.project.fraud_detection.rules.model.RuleResult;
import hackathon.project.fraud_detection.rules.model.RuleType;
import hackathon.project.fraud_detection.storage.entity.RuleEntity;
import hackathon.project.fraud_detection.storage.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEngine {

    private final RuleRepository ruleRepository;
    private final RuleFactory ruleFactory;
    private final CompositeRuleFactory compositeRuleFactory;

    public RuleEvaluationResult evaluate(TransactionRequest transaction) {
        List<RuleResult> ruleResults = new ArrayList<>();
        List<String> triggeredRuleNames = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        boolean isSuspicious = false;

        List<RuleEntity> rules = ruleRepository.findAll();
        rules.forEach(rule -> log.info("Found rule name: {}", rule.getName()));

        for (RuleEntity ruleEntity : rules) {
            if (!ruleEntity.isEnabled()) continue;
            if (ruleEntity.getType().equals(RuleType.THRESHOLD)) {
                var thresholdRule = ruleFactory.createRule(ruleEntity);
                log.info("Current threshold rule: {}", ruleEntity.getName());
                RuleResult thresholdEvaluationResult = thresholdRule.evaluate(transaction);
                ruleResults.add(thresholdEvaluationResult);
                if (thresholdEvaluationResult.triggered()) {
                    log.info("Threshold rule was triggered: {}", ruleEntity.getName());
                    triggeredRuleNames.add(ruleEntity.getName());
                    isSuspicious = true;
                    reasons.add(thresholdEvaluationResult.reason());
                }
            } if (ruleEntity.getType().equals(RuleType.PATTERN)) {
                //todo
            } if (ruleEntity.getType().equals(RuleType.COMPOSITE)) {
                val compositeRule = compositeRuleFactory.createCompositeRule(ruleEntity);
                log.info("Current composite rule: {}", ruleEntity.getName());
                RuleResult compositeEvaluationResult = compositeRule.evaluate(transaction);
                ruleResults.add(compositeEvaluationResult);
                if (compositeEvaluationResult.triggered()) {
                    log.info("Composite rule was triggered: {}", ruleEntity.getName());
                    triggeredRuleNames.add(ruleEntity.getName());
                    isSuspicious = true;
                    reasons.add(compositeEvaluationResult.reason());
                }
            } if (ruleEntity.getType().equals(RuleType.ML)) {
                //todo
            }
        }

        return new RuleEvaluationResult(isSuspicious, triggeredRuleNames, ruleResults, reasons);
    }
}
