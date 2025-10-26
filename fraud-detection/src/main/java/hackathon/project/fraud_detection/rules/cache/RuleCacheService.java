package hackathon.project.fraud_detection.rules.cache;

import hackathon.project.fraud_detection.api.dto.request.CreateRuleRequest;
import hackathon.project.fraud_detection.rules.engine.PatternRule;
import hackathon.project.fraud_detection.rules.engine.PatternRuleAnalyzer;
import hackathon.project.fraud_detection.rules.engine.PatternRuleAnalyzerStorage;
import hackathon.project.fraud_detection.rules.model.RuleType;
import hackathon.project.fraud_detection.rules.validator.PatternJsonParamsChecker;
import hackathon.project.fraud_detection.rules.validator.CompositeJsonParamsChecker;
import hackathon.project.fraud_detection.rules.validator.MLJsonParamsChecker;
import hackathon.project.fraud_detection.rules.validator.ThresholdJsonParamsChecker;
import hackathon.project.fraud_detection.storage.entity.RuleEntity;
import hackathon.project.fraud_detection.storage.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
@CacheConfig(cacheNames = {"allRules"})
public class RuleCacheService {

    private final RuleRepository ruleRepository;
    private final ThresholdJsonParamsChecker thresholdJsonParamsChecker;
    private final PatternJsonParamsChecker patternJsonParamsChecker;
    private final PatternRuleAnalyzerStorage patternRuleAnalyzerStorage;
    private final CompositeJsonParamsChecker compositeJsonParamsChecker;
    private final MLJsonParamsChecker mlJsonParamsChecker;


    @Cacheable(key = "'all'", value = "allRules")
    public List<RuleEntity> getAllRules() {
        log.info("Getting all rules");
        return ruleRepository.findAll();
    }

    @Caching(
            put = {
                    @CachePut(value = "ruleById", key = "#result.id"),
            },
            evict = {
                    @CacheEvict(value = "allRules", key = "'all'")
            }
    )
    public RuleEntity createRule(
            CreateRuleRequest request,
            UserDetails userDetails
    ) {
        RuleEntity newRule = new RuleEntity();
        newRule.setName(request.name());
        setRuleFields(newRule, request, userDetails);
        log.info("Creating new rule: {}", newRule.getName());
        ruleRepository.save(newRule);
        if (newRule.getType() == RuleType.PATTERN){
            PatternRuleAnalyzer patternRuleAnalyzer = new PatternRuleAnalyzer();
            PatternRule patternRule = new PatternRule(newRule.getId(), newRule.getPriority(), newRule.isEnabled(), newRule.getParams() , patternRuleAnalyzer);
            patternRuleAnalyzer.setPatternRule(patternRule);
            patternRuleAnalyzerStorage.addNewPatternRule(patternRuleAnalyzer);
        }
        return newRule;
    }

    @Caching(
            put = {
                    @CachePut(value = "ruleById", key = "#id"),
            },
            evict = {
                    @CacheEvict(value = "allRules", key = "'all'")
            }
    )
    public RuleEntity updateRule(
            CreateRuleRequest request,
            UUID id,
            UserDetails userDetails
    ) {
        RuleEntity updatedRule = ruleRepository.findRuleEntityById(id);
        RuleEntity newRule = new RuleEntity();
        setRuleFields(newRule, request, userDetails);
        if (updatedRule != null) {
            newRule.setVersion(updatedRule.getVersion() + 1);
            newRule.setName(updatedRule.getName()
                    .replaceAll("_v\\d+(?:_v\\d+)*$", "")
                    + "_v" + newRule.getVersion()
            );
        }
        log.info("Updating rule: {}", newRule.getName());
        ruleRepository.save(newRule);
        PatternRuleAnalyzer patternRuleAnalyzer;
        if (updatedRule.getType() == RuleType.PATTERN){
            for (PatternRuleAnalyzer analyzer : patternRuleAnalyzerStorage.getAnalyzers()) {
                PatternRule patternRule = analyzer.getPatternRule();
                if (patternRule != null && patternRule.getId().equals(updatedRule.getId())) {
                    patternRuleAnalyzer = analyzer;
                    patternRule = new PatternRule(updatedRule.getId(), updatedRule.getPriority(), updatedRule.isEnabled(), updatedRule.getParams() , patternRuleAnalyzer);
                    patternRuleAnalyzer.setPatternRule(patternRule);
                    patternRuleAnalyzerStorage.addNewPatternRule(patternRuleAnalyzer);
                    Map<String, Map<LocalDateTime, Integer>> map = new HashMap<>();
                    patternRuleAnalyzer.setTransactionMap(map);
                    break;
                }
            }
        }
        return updatedRule;
    }

    @Caching(
            put = {
                    @CachePut(value = "ruleById", key = "#id"),
            },
            evict = {
                    @CacheEvict(value = "allRules", key = "'all'")
            }
    )
    public RuleEntity setToggle(UUID id) {
        RuleEntity toggledRule = ruleRepository.findRuleEntityById(id);
        toggledRule.setEnabled(!toggledRule.isEnabled());
        log.info("Setting toggle for rule: {}", toggledRule.getId());
        PatternRuleAnalyzer patternRuleAnalyzer;
        if (toggledRule.getType() == RuleType.PATTERN){
            if(!toggledRule.isEnabled()) {
                for (PatternRuleAnalyzer analyzer : patternRuleAnalyzerStorage.getAnalyzers()) {
                    PatternRule patternRule = analyzer.getPatternRule();
                    if (patternRule != null && patternRule.getId().equals(toggledRule.getId())) {
                        patternRuleAnalyzer = analyzer;
                        patternRuleAnalyzerStorage.getAnalyzers().remove(patternRuleAnalyzer);
                        break;
                    }
                }
            }
            else{
                patternRuleAnalyzer = new PatternRuleAnalyzer();
                PatternRule patternRule = new PatternRule(toggledRule.getId(), toggledRule.getPriority(), toggledRule.isEnabled(), toggledRule.getParams() , patternRuleAnalyzer);
                patternRuleAnalyzer.setPatternRule(patternRule);
                patternRuleAnalyzerStorage.addNewPatternRule(patternRuleAnalyzer);
            }
        }
        ruleRepository.save(toggledRule);
        return toggledRule;
    }

    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "allRules", key = "'all'"),
                    @CacheEvict(value = "ruleById", key = "#id")
            }
    )
    public void deleteRule(UUID id) {
        log.info("Deleting rule: {}", id);
        ruleRepository.deleteRuleEntityById(id);
    }

    private void setRuleFields(
            RuleEntity ruleEntity,
            CreateRuleRequest request,
            UserDetails userDetails) {
        if (!checkJsonParams(request.params(), request.type())) {
            log.info("Request: {}, {}", request.params(), request.type());
            throw new IllegalArgumentException("Validation failed");
        }
        ruleEntity.setParams(request.params());
        ruleEntity.setType(request.type());
        ruleEntity.setPriority(request.priority());
        ruleEntity.setEnabled(request.enabled());
        ruleEntity.setUpdatedAt(LocalDateTime.now());
        ruleEntity.setUpdatedBy(userDetails.getUsername());
    }

    private boolean checkJsonParams(String params, RuleType ruleType) {
        return switch (ruleType) {
            case RuleType.THRESHOLD -> thresholdJsonParamsChecker.checkJsonParams(params, ruleType);
            case RuleType.PATTERN -> patternJsonParamsChecker.checkJsonParams(params, ruleType);
            case RuleType.COMPOSITE -> compositeJsonParamsChecker.checkJsonParams(params, ruleType);
            case RuleType.ML -> mlJsonParamsChecker.checkJsonParams(params, ruleType);
        };
    }
}
