ALTER TABLE emr_patient
    ADD COLUMN IF NOT EXISTS ssn VARCHAR(11);

CREATE UNIQUE INDEX IF NOT EXISTS uk_emr_patient_ssn ON emr_patient (ssn);
