package hackathon.project.fraud_detection.api;

import hackathon.project.fraud_detection.api.dto.request.CreateRuleRequest;
import hackathon.project.fraud_detection.rules.cache.RuleCacheService;
import hackathon.project.fraud_detection.storage.entity.RuleEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleCacheService ruleService;

    @GetMapping("/get-all")
    public ResponseEntity<List<RuleEntity>> getAllRules() {
        List<RuleEntity> rules = ruleService.getAllRules();
        return ResponseEntity.ok(rules);
    }

    @PostMapping("/create")
    public ResponseEntity<RuleEntity> createRule(
            @RequestBody CreateRuleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        RuleEntity newRule = ruleService.createRule(request, userDetails);
        return ResponseEntity.ok(newRule);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<RuleEntity> updateRule(
            @RequestBody CreateRuleRequest request,
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        RuleEntity updatedRule = ruleService.updateRule(request, id, userDetails);
        return ResponseEntity.ok(updatedRule);
    }

    @PatchMapping("/toggle/{id}")
    public ResponseEntity<RuleEntity> setToggle(@PathVariable UUID id) {
        RuleEntity toggledRule = ruleService.setToggle(id);
        return ResponseEntity.ok(toggledRule);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteRule(@PathVariable UUID id) {
        ruleService.deleteRule(id);
    }
}
