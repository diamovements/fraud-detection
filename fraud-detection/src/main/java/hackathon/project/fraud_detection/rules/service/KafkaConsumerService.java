package hackathon.project.fraud_detection.rules.service;

import hackathon.project.fraud_detection.api.dto.TransactionMessage;
import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.exceptions.DBWritingException;
import hackathon.project.fraud_detection.rules.model.RuleEvaluationResult;
import hackathon.project.fraud_detection.storage.entity.TransactionEntity;
import hackathon.project.fraud_detection.storage.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final RuleEngine ruleEngine;
    private final TransactionRepository transactionRepository;

    @KafkaListener(topics = "transaction_topic", groupId = "my-group")
    public void listenNotifications(TransactionMessage message) {
        log.info("Transaction was taken from queue "); //не создастся ли тут новый corellationId?
        TransactionEntity transaction = TransactionEntity.toTransactionEntity(message);
        TransactionRequest transactionRequest = new TransactionRequest(transaction);
        RuleEvaluationResult result = ruleEngine.evaluate(transactionRequest);
        transaction.setSuspicious(result.isSuspicious());
        transaction.setTriggeredRules(String.join(",", result.getTriggeredRuleIds().toString()));
        try {
            transactionRepository.save(transaction);
        } catch(Exception exception){
            log.info("ERROR: Ошибка обновления записи в БД");
        }
    }

}

