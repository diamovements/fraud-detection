package hackathon.project.fraud_detection.api;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.api.dto.response.TransactionResponse;
import hackathon.project.fraud_detection.exceptions.DBWritingException;
import hackathon.project.fraud_detection.rules.service.TransactionProcessingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionProcessingService transactionProcessingService;

    @PostMapping("/transactions")
    public ResponseEntity<?> processTransaction(@Valid @RequestBody TransactionRequest transactionRequest) {
        try {
            transactionProcessingService.processTransaction(transactionRequest);
            TransactionResponse response = TransactionResponse.accepted(MDC.get("correlationId"));
            return ResponseEntity.accepted().body(response);
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(TransactionResponse.error(e.getCause().getMessage()));
        }
    }
}

