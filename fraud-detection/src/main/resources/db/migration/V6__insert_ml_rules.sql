INSERT INTO fraud_rules (id, type, enabled, priority, params, version, updatedat, updatedby, name)
VALUES (
           gen_random_uuid(),
           'ML',
           true,
           1,
           '{"model_name": "log_reg", "operator": "GREATER_THAN_OR_EQUAL", "value": "0.75"}'
               ::jsonb,
           1,
           CURRENT_TIMESTAMP,
           'admin',
           'ml_classic'
       );