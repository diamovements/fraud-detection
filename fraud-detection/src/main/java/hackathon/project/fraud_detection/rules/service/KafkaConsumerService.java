package hackathon.project.fraud_detection.rules.service;

import hackathon.project.fraud_detection.api.dto.TransactionMessage;
import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.exceptions.DBWritingException;
import hackathon.project.fraud_detection.rules.model.RuleEvaluationResult;
import hackathon.project.fraud_detection.storage.entity.TransactionEntity;
import hackathon.project.fraud_detection.storage.entity.TransactionStatus;
import hackathon.project.fraud_detection.storage.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final RuleEngine ruleEngine;
    private final TransactionRepository transactionRepository;

    @KafkaListener(topics = "transaction_topic", groupId = "my-group")
    @Transactional
    public void listenNotifications(TransactionMessage message) {
        log.info("Transaction was taken from queue"); //не создастся ли тут новый corellationId?
        TransactionEntity transaction = TransactionEntity.toTransactionEntity(message);
        TransactionRequest transactionRequest = new TransactionRequest(transaction);
        RuleEvaluationResult result = ruleEngine.evaluate(transactionRequest);
        transaction.setSuspicious(result.isSuspicious());
        transaction.setStatus(result.isSuspicious() ? TransactionStatus.SUSPICIOUS : TransactionStatus.APPROVED);
        transaction.setTriggeredRules(String.join(",", result.getTriggeredRuleNames().toString()));

        log.info("Transaction was processed and marked as {}. Reasons: {}",
                transaction.getStatus(),
                result.getReason());

        try {
            transactionRepository.updateTransactionEntityById(
                    transaction.getOriginalTransactionId(),
                    transaction.getStatus(),
                    transaction.getTriggeredRules(),
                    transaction.getSuspicious()
            );
        } catch(Exception exception){
            log.info("ERROR: Ошибка обновления записи в БД: {}", exception.getMessage());
        }
    }
}

