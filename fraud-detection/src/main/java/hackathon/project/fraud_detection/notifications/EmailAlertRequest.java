package hackathon.project.fraud_detection.notifications;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record EmailAlertRequest(
        @JsonProperty("to_emails")
        String toEmails,
        @JsonProperty("transaction_id")
        String transactionId,
        String account,
        BigDecimal amount,
        @JsonProperty("ml_probability")
        double mlProbability,
        @JsonProperty("triggered_rules")
        List<String> triggeredRules,
        @JsonProperty("cc_emails")
        List<String> ccEmails

) {
}
