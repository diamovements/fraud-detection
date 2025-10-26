package hackathon.project.fraud_detection.api;

import hackathon.project.fraud_detection.api.dto.request.ChangeStatusRequest;
import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.api.dto.response.TransactionResponse;
import hackathon.project.fraud_detection.rules.service.ExporterService;
import hackathon.project.fraud_detection.rules.service.TransactionProcessingService;
import hackathon.project.fraud_detection.storage.entity.TransactionEntity;
import hackathon.project.fraud_detection.storage.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class TransactionController {

    private final TransactionProcessingService transactionProcessingService;
    private final TransactionRepository transactionRepository;
    private final ExporterService exporterService;

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

    @PutMapping("/transactions/change-status/{id}")
    public ResponseEntity<?> changeStatus(@PathVariable UUID id, @RequestBody @Valid ChangeStatusRequest changeStatusRequest) {
        try {
            transactionProcessingService.changeTransactionStatus(id, changeStatusRequest.status());

            log.info("Transaction with status {} has been updated", transactionRepository.findById(id).getStatus());
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error",
                    "Invalid status: " + changeStatusRequest.status()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/transactions/export")
    public ResponseEntity<?> exportAllTransactionsToCsv(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response) {

        log.info("Exporting all transactions to by user: {}", userDetails.getUsername());
        List<TransactionEntity> transactions = transactionRepository.findAll();

        try {
            exporterService.exportToCsv(transactions, response, userDetails);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(TransactionResponse.error(e.getCause().getMessage()));
        }
    }
}

