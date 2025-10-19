package hackathon.project.fraud_detection.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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

) { }
