package hackathon.project.fraud_detection.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import hackathon.project.fraud_detection.storage.entity.TransactionEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRequest(
    @NotNull
    @JsonProperty("transaction_id")
    String transactionId,

    @NotNull
    LocalDateTime timestamp,

    @NotBlank
    @JsonProperty("sender_account")
    String senderAccount,

    @NotBlank
    @JsonProperty("receiver_account")
    String receiverAccount,

    @NotNull
    BigDecimal amount,

    @NotBlank
    @JsonProperty("transaction_type")
    String transactionType,

    @JsonProperty("merchant_category")
    String merchantCategory,

    String location,

    @JsonProperty("device_used")
    String deviceUsed,

    @JsonProperty("time_since_last_transaction")
    @Nullable
    LocalDateTime timeSinceLastTransaction,

    @JsonProperty("payment_channel")
    String paymentChannel,

    @JsonProperty("ip_address")
    String ipAddress,

    @JsonProperty("device_hash")
    String deviceHash

) {
    public TransactionRequest(TransactionEntity transactionEntity){
        this(
                transactionEntity.getOriginalTransactionId(),
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
                transactionEntity.getDeviceHash()
        );
    }


}
