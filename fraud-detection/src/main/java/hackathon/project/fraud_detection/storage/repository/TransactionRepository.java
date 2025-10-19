package hackathon.project.fraud_detection.storage.repository;

import hackathon.project.fraud_detection.storage.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
}
