package hackathon.project.fraud_detection.storage.entity;

import hackathon.project.fraud_detection.api.dto.TransactionMessage;
import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transaction")
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "original_transaction_id", nullable = false, unique = true)
    private String originalTransactionId;

    @Column(name = "correlation_id", nullable = false, unique = true)
    private String correlationId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "sender_account", nullable = false)
    private String senderAccount;

    @Column(name = "receiver_account", nullable = false)
    private String receiverAccount;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(name = "merchant_category")
    private String merchantCategory;

    @Column(name = "location")
    private String location;

    @Column(name = "device_used")
    private String deviceUsed;

    @Column(name = "time_since_last_transaction")
    private LocalDateTime timeSinceLastTransaction;

    @Column(name = "payment_channel")
    private String paymentChannel;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "device_hash")
    private String deviceHash;

    @Column(name = "processed_at")
    @CreationTimestamp
    private LocalDateTime processedAt;

    @Column(name = "is_suspicious")
    private Boolean suspicious;

    @Column(name = "triggered_rules")
    private String triggeredRules;

    public static TransactionEntity toTransactionEntity(TransactionRequest transactionRequest) {
        TransactionEntity entity = new TransactionEntity();
        entity.setOriginalTransactionId(transactionRequest.transactionId());
        entity.setStatus(TransactionStatus.PROCESSING);
        entity.setAmount(transactionRequest.amount());
        entity.setTransactionType(transactionRequest.transactionType());
        entity.setSenderAccount(transactionRequest.senderAccount());
        entity.setReceiverAccount(transactionRequest.receiverAccount());
        entity.setTimestamp(transactionRequest.timestamp());
        entity.setLocation(transactionRequest.location());
        entity.setProcessedAt(null);
        entity.setIpAddress(transactionRequest.ipAddress());
        entity.setCorrelationId(MDC.get("correlationId"));
        entity.setDeviceHash(transactionRequest.deviceHash());
        entity.setDeviceUsed(transactionRequest.deviceUsed());
        entity.setTimeSinceLastTransaction(transactionRequest.timeSinceLastTransaction());
        entity.setPaymentChannel(transactionRequest.paymentChannel());
        entity.setMerchantCategory(transactionRequest.merchantCategory());

        return entity;
    }
    public static TransactionEntity toTransactionEntity(TransactionMessage message) {
        TransactionEntity entity = new TransactionEntity();
        entity.setOriginalTransactionId(message.originalTransactionId());
        entity.setStatus(message.status());
        entity.setAmount(message.amount());
        entity.setTransactionType(message.transactionType());
        entity.setSenderAccount(message.senderAccount());
        entity.setReceiverAccount(message.receiverAccount());
        entity.setTimestamp(message.timestamp());
        entity.setLocation(message.location());
        entity.setProcessedAt(message.processedAt());
        entity.setIpAddress(message.ipAddress());
        entity.setCorrelationId(message.correlationId());
        entity.setDeviceHash(message.deviceHash());
        entity.setDeviceUsed(message.deviceUsed());
        entity.setTimeSinceLastTransaction(message.timeSinceLastTransaction());
        entity.setPaymentChannel(message.paymentChannel());
        entity.setMerchantCategory(message.merchantCategory());

        return entity;
    }
}
