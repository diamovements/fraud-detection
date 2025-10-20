package hackathon.project.fraud_detection.storage.repository;

import hackathon.project.fraud_detection.storage.entity.RuleEntity;
import hackathon.project.fraud_detection.storage.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByLogin(String login);
    List<UserEntity> findAll();
}
