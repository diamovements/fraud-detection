INSERT INTO fraud_rules (id, type, enabled, priority, params, version, updatedat, updatedby, name)
VALUES (
           gen_random_uuid(),
           'COMPOSITE',
           true,
           1,
           '{"expr": "(amount_more_than_150 AND night_hours) OR (isAmountMore(200) AND transaction_is_withdrawal)"}'
               ::jsonb,
           1,
           CURRENT_TIMESTAMP,
           'admin',
           'composite1'
       );
INSERT INTO fraud_rules (id, type, enabled, priority, params, version, updatedat, updatedby, name)
VALUES (
           gen_random_uuid(),
           'COMPOSITE',
           true,
           1,
           '{"expr": "night_hours OR (isAmountMore(200) AND isSuspiciousMerchant)"}'
               ::jsonb,
           1,
           CURRENT_TIMESTAMP,
           'admin',
           'composite2'
       );