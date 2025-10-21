package hackathon.project.fraud_detection.rules.service;

import hackathon.project.fraud_detection.api.dto.TransactionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, TransactionMessage> kafkaTemplate;

    public void sendMessage(TransactionMessage message) {
        kafkaTemplate.send("transaction_topic", message);
    }
}
