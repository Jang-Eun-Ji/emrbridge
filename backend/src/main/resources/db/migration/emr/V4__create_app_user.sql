-- App users for JWT login (admin sees all hospitals; hospital users see one org)
CREATE TABLE IF NOT EXISTS app_user (
    id                       BIGSERIAL PRIMARY KEY,
    username                 VARCHAR(100) NOT NULL UNIQUE,
    password_hash            VARCHAR(255) NOT NULL,
    role                     VARCHAR(20)  NOT NULL,
    organization_synthea_id  VARCHAR(100),
    hospital_name            VARCHAR(300),
    enabled                  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_app_user_role CHECK (role IN ('ADMIN', 'HOSPITAL')),
    CONSTRAINT chk_hospital_org CHECK (
        (role = 'ADMIN' AND organization_synthea_id IS NULL)
        OR (role = 'HOSPITAL' AND organization_synthea_id IS NOT NULL)
    )
);

CREATE INDEX IF NOT EXISTS idx_app_user_org ON app_user(organization_synthea_id);
