package hackathon.project.fraud_detection.rules.engine;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.model.RuleResult;
import hackathon.project.fraud_detection.rules.validator.PatternJsonParamsChecker;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class PatternRuleAnalyzer {
    private PatternRule patternRule;
    private Map<String, Map<LocalDateTime, Integer>> transactionMap = new HashMap<>();

    public void updateMap(String by, LocalDateTime time){
        Map<LocalDateTime, Integer> countTimes = transactionMap.get(by);
        Integer count;
        if (countTimes == null) {
            count = 1;
        }
        else{
            count = countTimes.get(time.withSecond(0).withNano(0)) + 1;
        }
        countTimes.put(time.withSecond(0).withNano(0), count);
    }

    public boolean checkTransaction(String by, LocalDateTime time, int count){
        Map<LocalDateTime, Integer> countTimes = transactionMap.get(by);
        if (countTimes == null) {
            return false;
        }
        else{
            int sum = 0;
            for (Integer value : countTimes.values()) {
                sum += value;
            }
            return sum > count;
        }
    }

    public void cleanUp(){
        LocalDateTime windowMinutesAgo = LocalDateTime.now().minusMinutes((Integer) patternRule.getWindowMin());
        windowMinutesAgo.withSecond(0).withNano(0);
        transactionMap.values().forEach(innerMap ->
                innerMap.entrySet().removeIf(entry -> entry.getKey().isBefore(windowMinutesAgo))
        );
        transactionMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

}