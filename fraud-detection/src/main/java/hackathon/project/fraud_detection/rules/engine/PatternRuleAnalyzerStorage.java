package hackathon.project.fraud_detection.rules.engine;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Getter
@Setter
public class PatternRuleAnalyzerStorage {
    private final List<PatternRuleAnalyzer> analyzers;

    public PatternRuleAnalyzerStorage() {
        this.analyzers = new ArrayList<>();
    }

    public void addNewPatternRule(PatternRuleAnalyzer patternRuleAnalyzer){
        analyzers.add(patternRuleAnalyzer);
    }

    public PatternRuleAnalyzer getAnalyzerByRuleId(UUID ruleId) {
        return analyzers.stream()
                .filter(analyzer -> analyzer.getPatternRule() != null &&
                        analyzer.getPatternRule().getId().equals(ruleId))
                .findFirst()
                .orElse(null);
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void scheduledCleanup() {
        for (PatternRuleAnalyzer analyzer : analyzers) {
            analyzer.cleanUp();
        }
    }

}
