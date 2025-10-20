package hackathon.project.fraud_detection.api.dto.request;

public record SignUpRequest(String name, String surname, String login, String password) { }
