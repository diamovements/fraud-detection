package hackathon.project.fraud_detection.rules.service;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.engine.RuleContext;
import hackathon.project.fraud_detection.rules.engine.RuleFactory;
import hackathon.project.fraud_detection.rules.model.RuleEvaluationResult;
import hackathon.project.fraud_detection.rules.model.RuleResult;
import hackathon.project.fraud_detection.rules.model.RuleType;
import hackathon.project.fraud_detection.storage.entity.RuleEntity;
import hackathon.project.fraud_detection.storage.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEngine {

    private final RuleRepository ruleRepository;
    private final RuleContext ruleContext;
    private final RuleFactory ruleFactory;

    public RuleEvaluationResult evaluate(TransactionRequest transaction) {
        List<RuleResult> ruleResults = new ArrayList<>();
        List<UUID> triggeredRuleIds = new ArrayList<>();
        boolean isSuspicious = false;

        List<RuleEntity> rules = ruleRepository.findAll();
        rules.forEach(rule -> log.info("Found rule id: {}", rule.getId()));

        for (RuleEntity ruleEntity : rules) {
            if (!ruleEntity.isEnabled()) continue;
            if (ruleEntity.getType().equals(RuleType.THRESHOLD)) {
                var thresholdRule = ruleFactory.createRule(ruleEntity);
                log.info("Current threshold rule: {}", thresholdRule.getId());
                RuleResult thresholdEvaluationResult = thresholdRule.evaluate(transaction, ruleContext);
                ruleResults.add(thresholdEvaluationResult);
                if (thresholdEvaluationResult.triggered()) {
                    log.info("Threshold rule was triggered: {}", thresholdRule.getId());
                    triggeredRuleIds.add(thresholdRule.getId());
                    isSuspicious = true;
                }
            } else if (ruleEntity.getType().equals(RuleType.PATTERN)) {
                //todo
            } else if (ruleEntity.getType().equals(RuleType.COMPOSITE)) {
                //todo
            } else if (ruleEntity.getType().equals(RuleType.ML)) {
                //todo
            }
        }

        return new RuleEvaluationResult(isSuspicious, triggeredRuleIds, ruleResults);
    }
}
