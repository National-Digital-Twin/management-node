/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an error response to be sent to clients.
 * Contains a status code, an error message, and a unique error ID without exposing stack traces.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    /**
     * The HTTP status code
     */
    private int status;

    /**
     * A user-friendly error message
     */
    private String message;

    /**
     * A unique identifier for the error
     */
    private String errorId;
}
