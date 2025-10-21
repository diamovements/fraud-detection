package hackathon.project.fraud_detection.exceptions;

public class KafkaWritingError extends RuntimeException {
    public KafkaWritingError(String message) {
        super(message);
    }
}