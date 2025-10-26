SELECT 'CREATE DATABASE fraud_detection'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'fraud_detection')\gexec