CREATE TABLE fraud_rules (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     type VARCHAR(20) NOT NULL,
     enabled BOOLEAN NOT NULL DEFAULT true,
     priority INTEGER NOT NULL DEFAULT 100,
     params JSONB NOT NULL,
     version INTEGER NOT NULL DEFAULT 1,
     updatedat TIMESTAMP WITHOUT TIME ZONE NOT NULL,
     updatedby VARCHAR(100) NOT NULL
);