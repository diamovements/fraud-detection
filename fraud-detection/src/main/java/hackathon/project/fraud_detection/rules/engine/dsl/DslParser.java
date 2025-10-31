package hackathon.project.fraud_detection.rules.engine.dsl;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Component
public class DslParser {

    private final List<String> suspiciousMerchants = List.of("other", "online");

    public boolean isNighttime(TransactionRequest request) {
        return request.timestamp().toLocalTime().isBefore(LocalTime.of(6, 0))
                && request.timestamp().toLocalTime().isAfter(LocalTime.of(23, 0));
    }

    public boolean isAmountMore(BigDecimal amount, TransactionRequest request) {
        return amount.compareTo(request.amount()) >= 0;
    }

    public boolean isAmountLess(BigDecimal amount, TransactionRequest request) {
        return amount.compareTo(request.amount()) <= 0;
    }

    public boolean isSuspiciousMerchant(TransactionRequest request) {
        return suspiciousMerchants.contains(request.merchantCategory());
    }

}