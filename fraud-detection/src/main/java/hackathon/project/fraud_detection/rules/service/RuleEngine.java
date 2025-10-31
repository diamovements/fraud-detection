package hackathon.project.fraud_detection.rules.service;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.cache.RuleCacheService;
import hackathon.project.fraud_detection.rules.engine.*;
import hackathon.project.fraud_detection.rules.model.RuleEvaluationResult;
import hackathon.project.fraud_detection.rules.model.RuleResult;
import hackathon.project.fraud_detection.rules.model.RuleType;
import hackathon.project.fraud_detection.storage.entity.RuleEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEngine {

    private final RuleFactory ruleFactory;
    private final RuleCacheService ruleCacheService;
    private final PatternRuleAnalyzerStorage patternRuleAnalyzerStorage;
    private final CompositeRuleFactory compositeRuleFactory;
    private final Map<UUID, Rule> patternRuleCache = new ConcurrentHashMap<>();

    public RuleEvaluationResult evaluate(TransactionRequest transaction) {
        List<RuleResult> ruleResults = new ArrayList<>();
        List<String> triggeredRuleNames = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        boolean isSuspicious = false;

        List<RuleEntity> rules = ruleCacheService.getAllRules()
                .stream()
                .sorted(Comparator.comparing(RuleEntity::getPriority).reversed())
                .toList();
        rules.forEach(rule -> log.info("Found rule with name: {}", rule.getName()));

        for (RuleEntity ruleEntity : rules) {
            if (!ruleEntity.isEnabled()) continue;

            if (ruleEntity.getType().equals(RuleType.THRESHOLD)) {
                var thresholdRule = ruleFactory.createRule(ruleEntity);
                log.info("Current threshold rule: {}", ruleEntity.getName());
                isSuspicious = getEvaluationResult(thresholdRule, transaction, ruleResults, reasons,
                        ruleEntity, triggeredRuleNames);
            } else if (ruleEntity.getType().equals(RuleType.PATTERN)) {
                var patternRule = getOrCreatePatternRule(ruleEntity);
                log.info("Current pattern rule: {}", ruleEntity.getName());
                isSuspicious = getEvaluationResult(patternRule, transaction, ruleResults, reasons,
                        ruleEntity, triggeredRuleNames);
            } else if (ruleEntity.getType().equals(RuleType.COMPOSITE)) {
                var compositeRule = compositeRuleFactory.createCompositeRule(ruleEntity);
                log.info("Current composite rule: {}", ruleEntity.getName());
                isSuspicious = getEvaluationResult(compositeRule, transaction, ruleResults, reasons,
                        ruleEntity, triggeredRuleNames);
            } else if (ruleEntity.getType().equals(RuleType.ML)) {
                var mlRule = ruleFactory.createRule(ruleEntity);
                log.info("Current ml rule: {}", ruleEntity.getName());
                isSuspicious = getEvaluationResult(mlRule, transaction, ruleResults, reasons,
                        ruleEntity, triggeredRuleNames);
            }
        }
        return new RuleEvaluationResult(isSuspicious, triggeredRuleNames, ruleResults, reasons);
    }

    private boolean getEvaluationResult(Rule rule, TransactionRequest transaction,
                                     List<RuleResult> ruleResults, List<String> reasons,
                                     RuleEntity ruleEntity, List<String> triggeredRuleNames) {
        boolean suspicious = false;
        RuleResult mlEvaluationResult = rule.evaluate(transaction);
        ruleResults.add(mlEvaluationResult);
        if (mlEvaluationResult.triggered()) {
            log.info("Ml rule was triggered: {}", ruleEntity.getName());
            triggeredRuleNames.add(ruleEntity.getName());
            suspicious = true;
            reasons.add(mlEvaluationResult.reason());
        }
        return suspicious;
    }

    private Rule getOrCreatePatternRule(RuleEntity ruleEntity) {
        return patternRuleCache.computeIfAbsent(ruleEntity.getId(), id -> {
            PatternRuleAnalyzer analyzer = patternRuleAnalyzerStorage.getAnalyzerByRuleId(id);
            if (analyzer == null) {
                log.warn("PatternRuleAnalyzer not found for rule: {}, creating new", id);
                analyzer = new PatternRuleAnalyzer();
                patternRuleAnalyzerStorage.addNewPatternRule(analyzer);
            }
            return ruleFactory.createRule(ruleEntity);
        });
    }
}