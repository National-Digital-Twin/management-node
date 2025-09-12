/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.model.jwt;

import java.io.Serial;
import java.io.Serializable;

/**
 * Custom Principal object that includes clientId information from the JWT.
 *
 * @param subject  -- GETTER --
 *                 Get the subject (user identifier)
 * @param clientId -- GETTER --
 *                 Get the client ID
 */
public record EnhancedPrincipal(String subject, String clientId) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "CustomPrincipal{" + "subject='" + subject + '\'' + ", clientId='" + clientId + '\'' + '}';
    }
}
