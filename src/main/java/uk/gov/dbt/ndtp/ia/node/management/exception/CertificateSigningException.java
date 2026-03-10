/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.exception;

/**
 * Exception thrown when a certificate signing request is rejected.
 * Mapped to HTTP 403 by the global exception handler.
 */
public class CertificateSigningException extends RuntimeException {

    /**
     * Constructs a new CertificateSigningException with the specified detail message.
     *
     * @param message the detail message
     */
    public CertificateSigningException(String message) {
        super(message);
    }
}
