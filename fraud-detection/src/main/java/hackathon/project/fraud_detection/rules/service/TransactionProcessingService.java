package hackathon.project.fraud_detection.rules.service;

import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.rules.model.RuleEvaluationResult;
import hackathon.project.fraud_detection.storage.entity.TransactionEntity;
import hackathon.project.fraud_detection.storage.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class TransactionProcessingService {
    private final TransactionRepository transactionRepository;
    private final RuleEngine ruleEngine;

    public boolean processTransaction(TransactionRequest transactionRequest) {

        RuleEvaluationResult result = ruleEngine.evaluate(transactionRequest);

        if (result.isSuspicious()) {
            TransactionEntity transaction = TransactionEntity
                    .toTransactionEntity(transactionRequest);
            transaction.setCorrelationId(MDC.get("correlationId"));
            transaction.setSuspicious(result.isSuspicious());
            transaction.setTriggeredRules(String.join(",", result.getTriggeredRuleIds().toString()));

            transactionRepository.save(transaction);

        }
        log.info("Transaction processed: suspicious: {}, triggered rules ids: {}",
                result.isSuspicious(), result.getTriggeredRuleIds());

        return result.isSuspicious();
    }

}
