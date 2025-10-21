package hackathon.project.fraud_detection.api.dto;

import hackathon.project.fraud_detection.storage.entity.TransactionEntity;
import hackathon.project.fraud_detection.storage.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TransactionMessage(UUID id, TransactionStatus status, String originalTransactionId, String correlationId,
                                 LocalDateTime timestamp, String senderAccount, String receiverAccount, BigDecimal amount,
                                 String transactionType, String merchantCategory, String location, String deviceUsed,
                                 LocalDateTime timeSinceLastTransaction, String paymentChannel, String ipAddress,
                                 String deviceHash, LocalDateTime processedAt, Boolean suspicious, String triggeredRules,
                                 List<String> reason
) {
    public TransactionMessage(TransactionEntity transactionEntity){
        this(
                transactionEntity.getId(),
                transactionEntity.getStatus(),
                transactionEntity.getOriginalTransactionId(),
                transactionEntity.getCorrelationId(),
                transactionEntity.getTimestamp(),
                transactionEntity.getSenderAccount(),
                transactionEntity.getReceiverAccount(),
                transactionEntity.getAmount(),
                transactionEntity.getTransactionType(),
                transactionEntity.getMerchantCategory(),
                transactionEntity.getLocation(),
                transactionEntity.getDeviceUsed(),
                transactionEntity.getTimeSinceLastTransaction(),
                transactionEntity.getPaymentChannel(),
                transactionEntity.getIpAddress(),
                transactionEntity.getDeviceHash(),
                transactionEntity.getProcessedAt(),
                transactionEntity.getSuspicious(),
                transactionEntity.getTriggeredRules(),
                transactionEntity.getReason()
        );
    }
}