-- V2__create_synthea_emr_tables.sql
-- 1. organizations
CREATE TABLE emr_organization (
                                  id            BIGSERIAL PRIMARY KEY,
                                  synthea_id    VARCHAR(36) NOT NULL UNIQUE,
                                  name          VARCHAR(255),
                                  address       VARCHAR(255),
                                  city          VARCHAR(100),
                                  state         VARCHAR(50),
                                  zip           VARCHAR(20),
                                  lat           DOUBLE PRECISION,
                                  lon           DOUBLE PRECISION,
                                  phone         VARCHAR(30)
);

-- 2. providers
CREATE TABLE emr_provider (
                              id              BIGSERIAL PRIMARY KEY,
                              synthea_id      VARCHAR(36) NOT NULL UNIQUE,
                              organization_id BIGINT REFERENCES emr_organization(id),
                              name            VARCHAR(255),
                              gender          VARCHAR(10),
                              speciality      VARCHAR(100),
                              address         VARCHAR(255),
                              city            VARCHAR(100),
                              state           VARCHAR(50),
                              zip             VARCHAR(20),
                              lat             DOUBLE PRECISION,
                              lon             DOUBLE PRECISION,
                              utilization     INTEGER
);

-- 3. encounters
CREATE TABLE emr_encounter (
                               id              BIGSERIAL PRIMARY KEY,
                               synthea_id      VARCHAR(36) NOT NULL UNIQUE,
                               patient_id      BIGINT NOT NULL REFERENCES emr_patient(id),
                               organization_id BIGINT REFERENCES emr_organization(id),
                               provider_id     BIGINT REFERENCES emr_provider(id),
                               start_time      TIMESTAMP,
                               stop_time       TIMESTAMP,
                               encounter_class VARCHAR(50),
                               code            VARCHAR(20),
                               description     VARCHAR(500),
                               base_encounter_cost  NUMERIC(10,2),
                               total_claim_cost     NUMERIC(10,2),
                               payer_coverage       NUMERIC(10,2),
                               reason_code     VARCHAR(20),
                               reason_description VARCHAR(500)
);

-- 4. conditions
CREATE TABLE emr_condition (
                               id              BIGSERIAL PRIMARY KEY,
                               patient_id      BIGINT NOT NULL REFERENCES emr_patient(id),
                               encounter_id    BIGINT REFERENCES emr_encounter(id),
                               start_date      DATE,
                               stop_date       DATE,
                               code            VARCHAR(20),
                               description     VARCHAR(500)
);

-- 5. observations
CREATE TABLE emr_observation (
                                 id              BIGSERIAL PRIMARY KEY,
                                 patient_id      BIGINT NOT NULL REFERENCES emr_patient(id),
                                 encounter_id    BIGINT REFERENCES emr_encounter(id),
                                 obs_date        TIMESTAMP,
                                 category        VARCHAR(50),
                                 code            VARCHAR(50),
                                 description     VARCHAR(500),
                                 value           VARCHAR(255),
                                 units           VARCHAR(50),
                                 obs_type        VARCHAR(50)
);

-- 6. medications
CREATE TABLE emr_medication (
                                id                   BIGSERIAL PRIMARY KEY,
                                patient_id           BIGINT NOT NULL REFERENCES emr_patient(id),
                                encounter_id         BIGINT REFERENCES emr_encounter(id),
                                start_time           TIMESTAMP,
                                stop_time            TIMESTAMP,
                                code                 VARCHAR(20),
                                description          VARCHAR(500),
                                base_cost            NUMERIC(10,2),
                                payer_coverage       NUMERIC(10,2),
                                dispenses            INTEGER,
                                total_cost           NUMERIC(10,2),
                                reason_code          VARCHAR(20),
                                reason_description   VARCHAR(500)
);

-- 7. procedures
CREATE TABLE emr_procedure (
                               id              BIGSERIAL PRIMARY KEY,
                               patient_id      BIGINT NOT NULL REFERENCES emr_patient(id),
                               encounter_id    BIGINT REFERENCES emr_encounter(id),
                               proc_date       TIMESTAMP,
                               code            VARCHAR(20),
                               description     VARCHAR(500),
                               base_cost       NUMERIC(10,2),
                               reason_code     VARCHAR(20),
                               reason_description VARCHAR(500)
);

-- 8. allergies
CREATE TABLE emr_allergy (
                             id              BIGSERIAL PRIMARY KEY,
                             patient_id      BIGINT NOT NULL REFERENCES emr_patient(id),
                             encounter_id    BIGINT REFERENCES emr_encounter(id),
                             start_date      DATE,
                             stop_date       DATE,
                             code            VARCHAR(20),
                             system          VARCHAR(100),
                             description     VARCHAR(500),
                             allergy_type    VARCHAR(50),
                             category        VARCHAR(50),
                             reaction1_code  VARCHAR(20),
                             reaction1_desc  VARCHAR(255),
                             reaction2_code  VARCHAR(20),
                             reaction2_desc  VARCHAR(255)
);

-- 9. immunizations
CREATE TABLE emr_immunization (
                                  id              BIGSERIAL PRIMARY KEY,
                                  patient_id      BIGINT NOT NULL REFERENCES emr_patient(id),
                                  encounter_id    BIGINT REFERENCES emr_encounter(id),
                                  imm_date        TIMESTAMP,
                                  code            VARCHAR(20),
                                  description     VARCHAR(500),
                                  base_cost       NUMERIC(10,2)
);

-- 10. careplans
CREATE TABLE emr_careplan (
                              id              BIGSERIAL PRIMARY KEY,
                              synthea_id      VARCHAR(36) NOT NULL UNIQUE,
                              patient_id      BIGINT NOT NULL REFERENCES emr_patient(id),
                              encounter_id    BIGINT REFERENCES emr_encounter(id),
                              start_date      DATE,
                              stop_date       DATE,
                              code            VARCHAR(20),
                              description     VARCHAR(500),
                              reason_code     VARCHAR(20),
                              reason_description VARCHAR(500)
);