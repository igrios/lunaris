DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_class table_definition
          ON table_definition.oid = constraint_definition.conrelid
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = table_definition.oid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE table_definition.relname = 'localities'
          AND constraint_definition.contype = 'u'
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'name'
    ) THEN
        ALTER TABLE localities
            ADD CONSTRAINT uq_localities_name UNIQUE (name);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint constraint_definition
        JOIN pg_class table_definition
          ON table_definition.oid = constraint_definition.conrelid
        JOIN pg_attribute column_definition
          ON column_definition.attrelid = table_definition.oid
         AND column_definition.attnum = ANY (constraint_definition.conkey)
        WHERE table_definition.relname = 'fares'
          AND constraint_definition.contype = 'u'
          AND cardinality(constraint_definition.conkey) = 1
          AND column_definition.attname = 'locality_name'
    ) THEN
        ALTER TABLE fares
            ADD CONSTRAINT uq_fares_locality_name UNIQUE (locality_name);
    END IF;
END $$;

INSERT INTO localities (name, kms_to_cordoba, minutes_from_origin)
VALUES ('Arrufó', 344, -35)
ON CONFLICT (name) DO UPDATE
SET kms_to_cordoba = EXCLUDED.kms_to_cordoba,
    minutes_from_origin = EXCLUDED.minutes_from_origin;

INSERT INTO fares (id, locality_name, amount)
VALUES (gen_random_uuid(), 'Arrufó', 105000.00)
ON CONFLICT (locality_name) DO UPDATE
SET amount = EXCLUDED.amount;
