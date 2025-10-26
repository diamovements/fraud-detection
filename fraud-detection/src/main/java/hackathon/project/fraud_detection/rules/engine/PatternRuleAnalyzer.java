package hackathon.project.fraud_detection.rules.engine;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class PatternRuleAnalyzer {
    private PatternRule patternRule;
    private Map<String, Map<LocalDateTime, Integer>> transactionMap = new ConcurrentHashMap<>();

    public void updateMap(String by, LocalDateTime time) {
        if (by == null) by = "default";
        LocalDateTime minuteTime = time.withSecond(0).withNano(0);

        Map<LocalDateTime, Integer> countTimes = transactionMap.computeIfAbsent(
                by, k -> new ConcurrentHashMap<>()
        );
        Integer newCount = countTimes.merge(minuteTime, 1, Integer::sum);
    }

    public boolean checkTransaction(String by, LocalDateTime time, int minCount) {
        if (by == null) by = "default";
        Map<LocalDateTime, Integer> countTimes = transactionMap.get(by);
        if (countTimes == null || countTimes.isEmpty()) {
            return false;
        }
        LocalDateTime windowStart = time.minusMinutes(patternRule.getWindowMin());

        int sum = countTimes.entrySet().stream()
                .filter(entry -> !entry.getKey().isBefore(windowStart))
                .mapToInt(Map.Entry::getValue)
                .sum();

        log.info("Pattern check for {}: sum={}, minCount={}, window={} minutes, entries={}",
                by, sum, minCount, patternRule.getWindowMin(), countTimes.size());
        return sum >= minCount;
    }

    public void cleanUp() {
        if (patternRule == null || patternRule.getWindowMin() == null) {
            return;
        }
        LocalDateTime windowMinutesAgo = LocalDateTime.now().minusMinutes(patternRule.getWindowMin());

        transactionMap.forEach((key, innerMap) -> {
            innerMap.entrySet().removeIf(entry -> entry.getKey().isBefore(windowMinutesAgo));
        });
        transactionMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}