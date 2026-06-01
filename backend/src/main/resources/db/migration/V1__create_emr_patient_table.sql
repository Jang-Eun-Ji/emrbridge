CREATE TABLE emr_patient (
                             id BIGSERIAL PRIMARY KEY,

                             synthea_patient_id VARCHAR(100) NOT NULL UNIQUE,

                             first_name VARCHAR(100),
                             last_name VARCHAR(100),
                             gender VARCHAR(20),
                             birth_date DATE,
                             death_date DATE,

                             race VARCHAR(100),
                             ethnicity VARCHAR(100),

                             address TEXT,
                             city VARCHAR(100),
                             state VARCHAR(100),
                             county VARCHAR(100),
                             zip VARCHAR(20),

                             lat DOUBLE PRECISION,
                             lon DOUBLE PRECISION,

                             healthcare_expenses NUMERIC(15, 2),
                             healthcare_coverage NUMERIC(15, 2),

                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);