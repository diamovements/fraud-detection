package hackathon.project.fraud_detection.rules.ml;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.model.FraudPredictionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class MLClient {

    private final RestTemplate restTemplate;
    private final String url;

    public MLClient(
            RestTemplate restTemplate,
            @Value("${ml.service.url}") String url
    ) {
        this.restTemplate = restTemplate;
        this.url = url;
    }

    public FraudPredictionResult predictFraud(String modelName, TransactionRequest transactionRequest) {
        try {
            ResponseEntity<FraudPredictionResult> prediction = restTemplate.postForEntity(
                url + "/predict?model_name=" + modelName,
                transactionRequest,
                FraudPredictionResult.class
        );
            return prediction.getBody();
        } catch (Exception e) {
            log.error("Something went wrong during ML evaluation: {}", e.getMessage());
            return new FraudPredictionResult(modelName, 0.0, "ML error");
        }
    }
}
