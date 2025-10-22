INSERT INTO fraud_rules (id, type, enabled, priority, params, version, updatedat, updatedby, name)
VALUES (
           gen_random_uuid(),
           'THRESHOLD',
           true,
           1,
           '{
               "field": "amount",
               "operator": "GREATER_THAN",
               "value": "150.0"
           }'::jsonb,
           1,
           CURRENT_TIMESTAMP,
           'admin',
               'amount_more_than_150'
       );
INSERT INTO fraud_rules (id, type, enabled, priority, params, version, updatedat, updatedby, name)
VALUES (
           gen_random_uuid(),
           'THRESHOLD',
           true,
           1,
           '{
               "field": "timestamp",
               "operator": "LESS_THAN_OR_EQUAL",
               "value": "06:00"
           }'::jsonb,
           1,
           CURRENT_TIMESTAMP,
           'admin',
               'night_hours'
       );
INSERT INTO fraud_rules (id, type, enabled, priority, params, version, updatedat, updatedby, name)
VALUES (
           gen_random_uuid(),
           'THRESHOLD',
           true,
           1,
           '{
               "field": "transaction_type",
               "operator": "CONTAINS",
               "value": "withdrawal"
           }'::jsonb,
           1,
           CURRENT_TIMESTAMP,
           'admin',
               'transaction_is_withdrawal'
       );