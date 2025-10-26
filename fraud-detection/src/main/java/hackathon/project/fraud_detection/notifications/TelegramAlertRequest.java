package hackathon.project.fraud_detection.notifications;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record TelegramAlertRequest(
        @JsonProperty("transaction_id")
        String transactionId,
        String account,
        BigDecimal amount,
        @JsonProperty("ml_probability")
        double mlProbability,
        @JsonProperty("triggered_rules")
        List<String> triggeredRules,
        @JsonProperty("user_ids")
        List<String> userIds
) {
}
