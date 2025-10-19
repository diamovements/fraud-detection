package hackathon.project.fraud_detection.storage.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

import hackathon.project.fraud_detection.rules.model.RuleType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonbType;

@Entity
@Table(name = "fraud_rules")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private RuleType type;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(columnDefinition = "jsonb")
    @JdbcType(PostgreSQLJsonPGObjectJsonbType.class)
    private String params;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "updatedat", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updatedby", nullable = false)
    private String updatedBy;
}
