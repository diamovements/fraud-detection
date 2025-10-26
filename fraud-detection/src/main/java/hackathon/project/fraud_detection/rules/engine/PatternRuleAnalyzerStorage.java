package hackathon.project.fraud_detection.rules.engine;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Getter
@Setter
public class PatternRuleAnalyzerStorage {
    private final List<PatternRuleAnalyzer> analyzers;

    public PatternRuleAnalyzerStorage() {
        this.analyzers = new CopyOnWriteArrayList<>();
    }

    public void addNewPatternRule(PatternRuleAnalyzer patternRuleAnalyzer) {
        if (patternRuleAnalyzer != null) {
            analyzers.add(patternRuleAnalyzer);
        }
    }

    public PatternRuleAnalyzer getAnalyzerByRuleId(UUID ruleId) {
        return analyzers.stream()
                .filter(analyzer -> analyzer.getPatternRule() != null &&
                        ruleId.equals(analyzer.getPatternRule().getId()))
                .findFirst()
                .orElse(null);
    }

    @Scheduled(fixedRate = 60 * 1000)
    public void scheduledCleanup() {
        for (PatternRuleAnalyzer analyzer : analyzers) {
            try {
                analyzer.cleanUp();
            } catch (Exception e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }
    }
}