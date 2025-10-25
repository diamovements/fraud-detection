package hackathon.project.fraud_detection.rules.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FraudPredictionResult(
        @JsonProperty("model_used")
        String modelUsed,

        @JsonProperty("fraud_probability")
        double fraudProbability,
        String status
) {
}
