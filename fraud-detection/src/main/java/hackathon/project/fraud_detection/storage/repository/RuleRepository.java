package hackathon.project.fraud_detection.storage.repository;

import hackathon.project.fraud_detection.storage.entity.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, String> {
    RuleEntity findRuleEntityById(UUID id);
    RuleEntity findRuleEntityByName(String name);
}
