package hackathon.project.fraud_detection.security.exception;

public class ExpiredTokenException extends RuntimeException{
    public ExpiredTokenException(String message) {
        super(message);
    }
}
