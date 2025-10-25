package hackathon.project.fraud_detection.rules.model;

public record RuleResult(Boolean triggered,
                         String reason) { }
