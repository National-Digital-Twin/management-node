/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

ALTER TABLE organisation
ADD COLUMN certificate_automation_enabled BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE organisation_certificate (
    id BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT NOT NULL UNIQUE,
    subject_dn VARCHAR(500),
    serial_number VARCHAR(150),
    is_renewable BOOLEAN NOT NULL DEFAULT FALSE,
    renewal_ttl BIGINT,
    type VARCHAR(50) NOT NULL,
    requested_at TIMESTAMP,
    issued_at TIMESTAMP,
    expires_at TIMESTAMP,
    revoked_at TIMESTAMP,
    CONSTRAINT fk_organisation_certificate__organisation_id
    FOREIGN KEY (organisation_id) REFERENCES organisation (id)
);
CREATE INDEX idx_organisation_certificate__organisation_id
ON organisation_certificate (organisation_id);

CREATE TABLE certificate_events (
    id BIGSERIAL PRIMARY KEY,
    organisation_certificate_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    performed_by VARCHAR(255),
    CONSTRAINT fk_certificate_events__organisation_certificate_id
    FOREIGN KEY (organisation_certificate_id)
    REFERENCES organisation_certificate (id)
);
CREATE INDEX idx_certificate_events__organisation_certificate_id
ON certificate_events (organisation_certificate_id);

/* Mark existing orgs as manually configured */
UPDATE organisation SET certificate_automation_enabled = FALSE;
INSERT INTO organisation_certificate (
    organisation_id,
    subject_dn,
    serial_number,
    is_renewable,
    type,
    requested_at,
    issued_at,
    expires_at,
    revoked_at
)
SELECT
    id AS organisation_id,
    NULL AS subject_dn,
    NULL AS serial_number,
    FALSE AS is_renewable,
    'MANUAL' AS type,
    NULL AS requested_at,
    NULL AS issued_at,
    NULL AS expires_at,
    NULL AS revoked_at
FROM organisation;
