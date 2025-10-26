INSERT INTO fraud_rules (id, type, enabled, priority, params, version, updatedat, updatedby, name)
VALUES (
           gen_random_uuid(),
           'PATTERN',
           true,
           1,
           '{"by": "location", "windowMin": 15, "operator": "EQUAL", "field": "transaction_type", "value" : "withdrawal", "minCount": 4}'
               ::jsonb,
           1,
           CURRENT_TIMESTAMP,
           'admin',
           'pattern1'
       );
INSERT INTO fraud_rules (id, type, enabled, priority, params, version, updatedat, updatedby, name)
VALUES (
           gen_random_uuid(),
           'PATTERN',
           true,
           1,
           '{"by": "sender_account", "windowMin": 10, "operator": "GREATER_THAN_OR_EQUAL", "field": "amount", "value" : 500, "minCount": 2}'
               ::jsonb,
           1,
           CURRENT_TIMESTAMP,
           'admin',
           'pattern2'
       );
INSERT INTO fraud_rules (id, type, enabled, priority, params, version, updatedat, updatedby, name)
VALUES (
           gen_random_uuid(),
           'PATTERN',
           true,
           1,
           '{"by": "ip_address", "windowMin": 2, "operator": "LESS_THAN", "field": "amount", "value" : 300, "minCount": 10}'
               ::jsonb,
           1,
           CURRENT_TIMESTAMP,
           'admin',
           'pattern3'
       );