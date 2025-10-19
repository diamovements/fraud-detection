package hackathon.project.fraud_detection.api.dto.response;

//в дальнейшем будет использоваться для обработки разных ошибок
public record ErrorResponse(
    String message,
    int statusCode,
    String resultCode
) {

    public static ErrorResponse validationError() {
        return new ErrorResponse("Transaction validation failed",
                400, "Bad Request");
    }

    public static ErrorResponse serverError() {
        return new ErrorResponse("Server Error",
                500, "Internal Server Error");
    }

    public static ErrorResponse notFound() {
        return new ErrorResponse("Not Found",
                404, "Not Found");
    }
}
