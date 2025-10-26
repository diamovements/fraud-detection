package hackathon.project.fraud_detection.rules.service;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.cache.RuleCacheService;
import hackathon.project.fraud_detection.rules.engine.*;
import hackathon.project.fraud_detection.rules.engine.CompositeRuleFactory;
import hackathon.project.fraud_detection.rules.engine.Rule;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEngine {

    private final RuleFactory ruleFactory;
    private final RuleCacheService ruleCacheService;
    private final PatternRuleAnalyzerStorage patternRuleAnalyzerStorage;
    private final CompositeRuleFactory compositeRuleFactory;

    public RuleEvaluationResult evaluate(TransactionRequest transaction) {
        List<RuleResult> ruleResults = new ArrayList<>();
        List<String> triggeredRuleNames = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        boolean isSuspicious = false;

        List<RuleEntity> rules = ruleCacheService.getAllRules()
                .stream()
                .sorted(Comparator.comparing(RuleEntity::getPriority).reversed())
                .toList();
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
            } else if (ruleEntity.getType().equals(RuleType.PATTERN)) {
                var patternRule = ruleFactory.createRule(ruleEntity);
                log.info("Current pattern rule: {}", ruleEntity.getName());
                PatternRuleAnalyzer analyzer = patternRuleAnalyzerStorage.getAnalyzerByRuleId(ruleEntity.getId());
                if (analyzer == null) {
                    log.warn("PatternRuleAnalyzer not found for rule: {}", ruleEntity.getId());
                    continue;
                }
                RuleResult patternEvaluationResult = patternRule.evaluate(transaction);
                ruleResults.add(patternEvaluationResult);
                if (patternEvaluationResult.triggered()) {
                    log.info("Pattern rule was triggered: {}", ruleEntity.getName());
                    triggeredRuleNames.add(ruleEntity.getName());
                    isSuspicious = true;
                    reasons.add(patternEvaluationResult.reason());
                }
            } else if (ruleEntity.getType().equals(RuleType.COMPOSITE)) {
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
            } else if (ruleEntity.getType().equals(RuleType.ML)) {
                val mlRule = ruleFactory.createRule(ruleEntity);
                log.info("Current ml rule: {}", ruleEntity.getName());
                RuleResult mlEvaluationResult = mlRule.evaluate(transaction);
                ruleResults.add(mlEvaluationResult);
                if (mlEvaluationResult.triggered()) {
                    log.info("Ml rule was triggered: {}", ruleEntity.getName());
                    triggeredRuleNames.add(ruleEntity.getName());
                    isSuspicious = true;
                    reasons.add(mlEvaluationResult.reason());
                }
            }
        }

        return new RuleEvaluationResult(isSuspicious, triggeredRuleNames, ruleResults, reasons);
    }
}
