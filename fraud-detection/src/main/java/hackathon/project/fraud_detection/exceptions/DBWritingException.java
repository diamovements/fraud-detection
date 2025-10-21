package hackathon.project.fraud_detection.exceptions;

public class DBWritingException extends RuntimeException {
    public DBWritingException(String message) {
        super(message);
    }
}