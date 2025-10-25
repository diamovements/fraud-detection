package hackathon.project.fraud_detection.api;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.api.dto.response.TransactionResponse;
import hackathon.project.fraud_detection.exceptions.DBWritingException;
import hackathon.project.fraud_detection.rules.service.TransactionProcessingService;
import hackathon.project.fraud_detection.storage.entity.TransactionEntity;
import hackathon.project.fraud_detection.storage.repository.TransactionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class TransactionController {

    private final TransactionProcessingService transactionProcessingService;
    private final TransactionRepository transactionRepository;

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

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionEntity>> getTransactions() {
        var transactions = transactionRepository.findAll();
        return ResponseEntity.ok().body(transactions);
    }
}

