package hackathon.project.fraud_detection.storage.repository;

import hackathon.project.fraud_detection.storage.entity.TransactionEntity;
import hackathon.project.fraud_detection.storage.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

    @Modifying
    @Query("UPDATE TransactionEntity t SET " +
            "t.status = :status, t.triggeredRules = :triggeredRules, t.suspicious = :suspicious " +
            "WHERE t.originalTransactionId = :original_transaction_id")
    void updateTransactionEntityById(@Param("original_transaction_id") String original_transaction_id,
                                     @Param("status") TransactionStatus status,
                                     @Param("triggeredRules") String triggeredRules,
                                     @Param("suspicious") boolean suspicious);

    @Modifying
    @Query("UPDATE TransactionEntity t SET " +
            "t.status = :status, t.suspicious = :suspicious " +
            "WHERE t.id = :id")
    void updateTransactionStatus(@Param("id") UUID id,
                                 @Param("status") TransactionStatus status,
                                 @Param("suspicious") boolean suspicious);
    TransactionEntity findById(UUID id);
}
