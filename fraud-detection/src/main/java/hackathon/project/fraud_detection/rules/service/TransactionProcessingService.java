package hackathon.project.fraud_detection.rules.service;

import hackathon.project.fraud_detection.api.dto.TransactionMessage;
import hackathon.project.fraud_detection.api.dto.request.TransactionRequest;
import hackathon.project.fraud_detection.exceptions.DBWritingException;
import hackathon.project.fraud_detection.exceptions.KafkaWritingError;
import hackathon.project.fraud_detection.rules.model.RuleEvaluationResult;
import hackathon.project.fraud_detection.storage.entity.TransactionEntity;
import hackathon.project.fraud_detection.storage.entity.TransactionStatus;
import hackathon.project.fraud_detection.storage.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class TransactionProcessingService {
    private final TransactionRepository transactionRepository;
    private final KafkaProducerService kafkaProducerService;

    public void processTransaction(TransactionRequest transactionRequest) {

        // валидация (+ как защита от ретраев включить проверку что id транзакции не совпадает с имеющими в кэше)
        // если валидация пройдена, а иначе вернуть 400
        TransactionEntity transaction = TransactionEntity
                .toTransactionEntity(transactionRequest);
        transaction.setCorrelationId(MDC.get("correlationId"));

        try {
            transactionRepository.save(transaction);
        } catch(Exception exception){
            log.info("ERROR: Ошибка записи в БД");
            throw new DBWritingException("Ошибка записи в БД");
        }

        try {
            kafkaProducerService.sendMessage(new TransactionMessage(transaction));
        } catch (Exception exception){
            log.info("ERROR: Ошибка записи в очередь");
            throw new KafkaWritingError("Ошибка записи в очередь");
        }

    }

    @Transactional
    public void changeTransactionStatus(UUID transactionId, String newStatusStr) {
        try {
            TransactionStatus newStatus = TransactionStatus.valueOf(newStatusStr);
            boolean shouldMarkAsNotSuspicious = newStatus == TransactionStatus.REVIEWED;

            transactionRepository.updateTransactionStatus(
                    transactionId,
                    newStatus,
                    shouldMarkAsNotSuspicious
            );
        } catch (Exception e) {
            log.error("Error updating transaction status", e);
            throw e;
        }
    }

}
