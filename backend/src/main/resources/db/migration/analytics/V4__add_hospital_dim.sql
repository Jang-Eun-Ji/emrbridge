-- ============================================================
-- V4__add_hospital_dim.sql
-- Purpose: Add hospital dimension so analytics facts can be
--          filtered per hospital (hospital admin) or across all
--          hospitals (platform admin).
-- ============================================================

CREATE TABLE IF NOT EXISTS hospital_dim (
    hospital_id SERIAL PRIMARY KEY,
    original_organization_id VARCHAR(100) UNIQUE NOT NULL, -- FHIR Organization identifier (Synthea org UUID)
    hospital_name VARCHAR(300) NOT NULL,
    city VARCHAR(100),
    state VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_hospital_name ON hospital_dim(hospital_name);
CREATE INDEX IF NOT EXISTS idx_hospital_original_org ON hospital_dim(original_organization_id);

ALTER TABLE visit_facts
    ADD COLUMN IF NOT EXISTS hospital_id INT REFERENCES hospital_dim(hospital_id);

CREATE INDEX IF NOT EXISTS idx_visit_hospital ON visit_facts(hospital_id);

-- Recreate summary views with hospital so Power BI / APIs can filter by hospital
DROP VIEW IF EXISTS monthly_visit_summary;
CREATE VIEW monthly_visit_summary AS
SELECT
    hd.hospital_id,
    hd.hospital_name,
    td.year_month,
    td.year,
    td.month,
    COUNT(*) AS total_visits,
    COUNT(DISTINCT vf.patient_id) AS unique_patients
FROM visit_facts vf
         JOIN time_dim td ON vf.time_id = td.time_id
         LEFT JOIN hospital_dim hd ON vf.hospital_id = hd.hospital_id
GROUP BY hd.hospital_id, hd.hospital_name, td.time_id, td.year_month, td.year, td.month
ORDER BY hd.hospital_name NULLS LAST, td.year_month;

DROP VIEW IF EXISTS disease_distribution_summary;
CREATE VIEW disease_distribution_summary AS
SELECT
    hd.hospital_id,
    hd.hospital_name,
    dd.disease_code,
    dd.disease_name,
    dd.disease_category,
    COUNT(DISTINCT vf.patient_id) AS patient_count,
    SUM(vf.visit_count) AS visit_count
FROM visit_facts vf
         JOIN disease_dim dd ON vf.disease_id = dd.disease_id
         LEFT JOIN hospital_dim hd ON vf.hospital_id = hd.hospital_id
GROUP BY hd.hospital_id, hd.hospital_name, dd.disease_id, dd.disease_code, dd.disease_name, dd.disease_category
ORDER BY visit_count DESC;

DROP VIEW IF EXISTS region_distribution_summary;
CREATE VIEW region_distribution_summary AS
SELECT
    hd.hospital_id,
    hd.hospital_name,
    rd.state,
    rd.city,
    rd.region_name,
    COUNT(DISTINCT vf.patient_id) AS patient_count,
    SUM(vf.visit_count) AS visit_count
FROM visit_facts vf
         JOIN region_dim rd ON vf.region_id = rd.region_id
         LEFT JOIN hospital_dim hd ON vf.hospital_id = hd.hospital_id
GROUP BY hd.hospital_id, hd.hospital_name, rd.region_id, rd.state, rd.city, rd.region_name
ORDER BY visit_count DESC;

DROP VIEW IF EXISTS patient_demographics_summary;
CREATE VIEW patient_demographics_summary AS
SELECT
    hd.hospital_id,
    hd.hospital_name,
    pd.age_group,
    pd.gender,
    COUNT(DISTINCT pd.patient_id) AS patient_count,
    SUM(vf.visit_count) AS visit_count
FROM visit_facts vf
         JOIN patient_dim pd ON vf.patient_id = pd.patient_id
         LEFT JOIN hospital_dim hd ON vf.hospital_id = hd.hospital_id
GROUP BY hd.hospital_id, hd.hospital_name, pd.age_group, pd.gender
ORDER BY hd.hospital_name NULLS LAST, pd.age_group, pd.gender;
