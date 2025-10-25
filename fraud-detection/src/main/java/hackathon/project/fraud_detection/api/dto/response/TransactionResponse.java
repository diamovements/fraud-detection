package hackathon.project.fraud_detection.api.dto.response;

public record TransactionResponse(
    String status,
    String correlationId,
    String message
) {

    public static TransactionResponse accepted(String correlationId) {
        return new TransactionResponse("ACCEPTED", correlationId,
                "Transaction accepted for processing");
    }

    public static TransactionResponse error(String message) {
        return new TransactionResponse("ERROR", null, message);
    }
}
