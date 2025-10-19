package hackathon.project.fraud_detection.storage.repository;

import hackathon.project.fraud_detection.rules.model.RuleType;
import hackathon.project.fraud_detection.storage.entity.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//хранилище правил, репозиторий по умолчанию предоставляет реализацию классических
// запросов sql, так что нам не придется их писать самим
@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, String> {
    RuleEntity findRuleEntityByType(RuleType ruleType);
}
