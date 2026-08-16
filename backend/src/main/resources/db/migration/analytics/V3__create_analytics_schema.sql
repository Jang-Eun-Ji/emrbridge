-- ============================================================
-- V3__create_analytics_schema.sql
-- Purpose: Create Star Schema for hospital analytics
-- Author: Analytics Team
-- Version: 1.0
-- Description:
--   Builds dimensional model (Star Schema) for analyzing:
--   - Patient demographics (age group, gender, region)
--   - Disease distribution
--   - Monthly visit trends
--   - Geographic patient distribution
-- ============================================================

-- ============================================================
-- 1. TIME DIMENSION
-- Purpose: Support monthly analysis with year, quarter info
-- PK: time_id
-- ============================================================
/**
 * time_dim table
 * Purpose: monthly time dimension for analytics, including year, month, quarter, and YYYY-MM format
 * Structure: year, month, quarter, and YYYY-MM format
 * PK: time_id (auto-incremented)
 * Example: 2024 3 → time_id=1, year=2024, month=3, quarter=Q1, year_month='2024-03'
 */
CREATE TABLE IF NOT EXISTS time_dim (
                                        time_id SERIAL PRIMARY KEY,
                                        year INT NOT NULL,
                                        month INT NOT NULL,
                                        quarter INT NOT NULL,
                                        year_month VARCHAR(7) UNIQUE NOT NULL, -- 'YYYY-MM' 형식
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Index for performance
CREATE INDEX idx_time_year_month ON time_dim(year_month);

-- ============================================================
-- 2. REGION DIMENSION
-- Purpose: Support geographic analysis
-- PK: region_id
-- FK: None (Dimension table)
-- ============================================================
/**
 * region_dim table
 * Purpose: Store patient geographic information hierarchically (State → County → City)
 * Structure: Separate state, county, and city for analysis at different levels
 * PK: region_id (auto-incremented)
 * Example: California → San Francisco County → San Francisco
 * Note: region_name is used for display purposes in analysis
 */
CREATE TABLE IF NOT EXISTS region_dim (
                                          region_id SERIAL PRIMARY KEY,
                                          state VARCHAR(100) NOT NULL,              -- 주/도 (State)
    county VARCHAR(100),                       -- 카운티/도시 상위 구분
    city VARCHAR(100) NOT NULL,                -- 도시 (City)
    region_name VARCHAR(300) UNIQUE NOT NULL, -- 'California, San Francisco' 형식
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Index for performance
CREATE INDEX idx_region_state ON region_dim(state);
CREATE INDEX idx_region_city ON region_dim(city);
CREATE INDEX idx_region_name ON region_dim(region_name);

-- ============================================================
-- 3. PATIENT DIMENSION
-- Purpose: Store patient demographic attributes
-- PK: patient_id
-- FK: region_id
-- ============================================================
/**
 * patient_dim table
 * Purpose: Store patient demographic information and automatically calculate age_group
 * Structure:
 *   - original_patient_id: Reference to emr_patient.id in the EMR DB
 *   - gender: 'M' or 'F'
 *   - age_group: '0-10', '11-20', ..., '70+' (automatically calculated from birth_date)
 *   - region_id: Reference to region_dim
 * PK: patient_id (auto-incremented for analysis)
 * Example:
 *   - Birthdate 1990-05-15 → age=34 → age_group='31-40'
 *   - Address "123 Main St, San Francisco, CA" → region_id reference
 */
CREATE TABLE IF NOT EXISTS patient_dim (
                                           patient_id SERIAL PRIMARY KEY,
                                           original_patient_id BIGINT NOT NULL,      -- EMR DB의 emr_patient.id (참조용)
                                           first_name VARCHAR(100),
    last_name VARCHAR(100),
    gender VARCHAR(10),                        -- 'M', 'F'
    birth_date DATE NOT NULL,
    age INT,                                   -- 계산된 현재 나이 (분석 시점 기준)
    age_group VARCHAR(20),                     -- '0-10', '11-20', ..., '70+' (분석용)
    region_id INT REFERENCES region_dim(region_id),
    registration_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraint: gender 'M' or 'F'
    CONSTRAINT chk_gender CHECK (gender IN ('M', 'F')),

    -- Constraint: original_patient_id is unique (no duplicates)
    UNIQUE(original_patient_id)
    );

-- Index for performance (frequently filtered columns in analysis)
CREATE INDEX idx_patient_age_group ON patient_dim(age_group);
CREATE INDEX idx_patient_gender ON patient_dim(gender);
CREATE INDEX idx_patient_region ON patient_dim(region_id);
CREATE INDEX idx_patient_original_id ON patient_dim(original_patient_id);

-- ============================================================
-- 4. DISEASE DIMENSION
-- Purpose: Store disease/condition information
-- PK: disease_id
-- FK: None (Dimension table)
-- ============================================================
/**
 * disease_dim table
 * Purpose: Store disease/condition information
 * Structure:
 *   - disease_code: ICD-10 code (medical standard)
 *   - disease_name: Disease name
 *   - disease_category: Category (e.g., 'Respiratory', 'Cardiovascular')
 * PK: disease_id (auto-incremented for analysis)
 * Example:
 *   - code='I10', name='Essential hypertension', category='Cardiovascular'
 *   - code='J44.9', name='Chronic obstructive pulmonary disease', category='Respiratory'
 */
CREATE TABLE IF NOT EXISTS disease_dim (
                                           disease_id SERIAL PRIMARY KEY,
                                           disease_code VARCHAR(20) UNIQUE,           -- ICD-10 code
    disease_name VARCHAR(500) NOT NULL,
    disease_category VARCHAR(100),             -- Category
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Index for performance
CREATE INDEX idx_disease_code ON disease_dim(disease_code);
CREATE INDEX idx_disease_category ON disease_dim(disease_category);

-- ============================================================
-- 5. VISIT FACTS
-- Purpose: Central fact table for all analytics
-- PK: visit_id
-- FK: patient_id, disease_id, time_id, region_id
-- ============================================================
/**
 * visit_facts table
 * Purpose: Central fact table for all visit records
 * Structure:
 *   - Each row represents a single visit record
 *   - All analyses (patient, disease, time, region) join around this table
 *   - visit_count: Number of visits (usually 1, multiple visits are represented by multiple rows)
 * FK Relationships (Star Schema):
 *   - patient_id → patient_dim (Patient information)
 *   - disease_id → disease_dim (Disease information)
 *   - time_id → time_dim (Visit time)
 *   - region_id → region_dim (Patient residence region)
 * PK: visit_id (auto-incremented)
 * Example:
 *   - 2024-03-15 John (age 42, M, CA) → Essential Hypertension (I10) → 1 visit
 */
CREATE TABLE IF NOT EXISTS visit_facts (
                                           visit_id SERIAL PRIMARY KEY,

    -- Foreign Keys (Star Schema joins)
                                           patient_id INT NOT NULL REFERENCES patient_dim(patient_id),
    disease_id INT NOT NULL REFERENCES disease_dim(disease_id),
    time_id INT NOT NULL REFERENCES time_dim(time_id),
    region_id INT NOT NULL REFERENCES region_dim(region_id),

    -- Fact data
    visit_date DATE NOT NULL,                  -- Visit date
    visit_count INT DEFAULT 1,                 -- Number of visits (metric)
    visit_type VARCHAR(50),                    -- 'first visit' (initial) vs 'follow-up' (follow-up)
    original_encounter_id BIGINT,              -- EMR DB's emr_encounter.id (for reference)

-- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraint: visit_count has to be positive
    CONSTRAINT chk_visit_count CHECK (visit_count > 0)
    );

-- Index for performance (filtered columns in analysis)
CREATE INDEX idx_visit_patient ON visit_facts(patient_id);
CREATE INDEX idx_visit_disease ON visit_facts(disease_id);
CREATE INDEX idx_visit_time ON visit_facts(time_id);
CREATE INDEX idx_visit_region ON visit_facts(region_id);
CREATE INDEX idx_visit_date ON visit_facts(visit_date);
CREATE INDEX idx_visit_encounter ON visit_facts(original_encounter_id);

-- ============================================================
-- 6. SUMMARY VIEWS (analytics optimized for Power BI)
-- Purpose: Pre-aggregated data for faster Power BI queries
-- ============================================================

/**
 * monthly_visit_summary view
 * Purpose: Monthly visit summary (optimized for Power BI)
 * Columns:
 *   - year_month: Month in 'YYYY-MM' format
 *   - total_visits: Total number of visits in the month
 *   - unique_patients: Number of unique patients in the month
 * Usage: Directly used in Power BI's monthly trend charts
 */
CREATE VIEW monthly_visit_summary AS
SELECT
    td.year_month,
    td.year,
    td.month,
    COUNT(*) AS total_visits,                  -- Total number of visits
    COUNT(DISTINCT vf.patient_id) AS unique_patients  -- Number of unique patients
FROM visit_facts vf
         JOIN time_dim td ON vf.time_id = td.time_id
GROUP BY td.time_id, td.year_month, td.year, td.month
ORDER BY td.year_month;

/**
 * disease_distribution_summary view
 * Purpose: Disease-wise patient distribution summary
 * Columns:
 *   - disease_code: Disease code
 *   - disease_name: Disease name
 *   - patient_count: Number of patients with the disease
 *   - visit_count: Total number of visits for the disease
 * Usage: Power BI's disease distribution chart
 */
CREATE VIEW disease_distribution_summary AS
SELECT
    dd.disease_code,
    dd.disease_name,
    dd.disease_category,
    COUNT(DISTINCT vf.patient_id) AS patient_count,  -- Number of patients with the disease
    SUM(vf.visit_count) AS visit_count               -- Total number of visits for the disease
FROM visit_facts vf
         JOIN disease_dim dd ON vf.disease_id = dd.disease_id
GROUP BY dd.disease_id, dd.disease_code, dd.disease_name, dd.disease_category
ORDER BY visit_count DESC;

/**
 * region_distribution_summary view
 * Purpose: Region-wise patient distribution summary
 * Columns:
 *   - state
 *   - city
 *   - patient_count: Number of patients in the region
 *   - visit_count: Total number of visits for patients in the region
 * Usage: Power BI's region distribution map/chart
 */
CREATE VIEW region_distribution_summary AS
SELECT
    rd.state,
    rd.city,
    rd.region_name,
    COUNT(DISTINCT vf.patient_id) AS patient_count,  -- Number of patients in the region
    SUM(vf.visit_count) AS visit_count               -- Total number of visits for patients in the region
FROM visit_facts vf
         JOIN region_dim rd ON vf.region_id = rd.region_id
GROUP BY rd.region_id, rd.state, rd.city, rd.region_name
ORDER BY visit_count DESC;

/**
 * patient_demographics_summary view
 * Purpose: Patient demographics summary
 * Columns:
 *   - age_group: Age group ('0-10', '11-20', ...)
 *   - gender: Gender ('M', 'F')
 *   - patient_count: Number of patients in the group
 *   - visit_count: Total number of visits for the group
 * Usage: Power BI's demographics dashboard
 */
CREATE VIEW patient_demographics_summary AS
SELECT
    pd.age_group,
    pd.gender,
    COUNT(DISTINCT pd.patient_id) AS patient_count,  -- Number of patients in the group
    SUM(vf.visit_count) AS visit_count               -- Total number of visits for the group
FROM visit_facts vf
         JOIN patient_dim pd ON vf.patient_id = pd.patient_id
GROUP BY pd.age_group, pd.gender
ORDER BY pd.age_group, pd.gender;

-- ============================================================
-- MIGRATION COMPLETE
-- Next Step: Run ETL script to populate data from EMR DB
-- ============================================================