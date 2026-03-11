/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.providers.certificate;

import uk.gov.dbt.ndtp.ia.node.management.model.dto.certificates.SignCertResponseDTO;

/**
 * Provides certificate signing orchestration, including record updates and audit events.
 */
public interface CertificateSigningProvider {

    /**
     * Sign a CSR and record the result against the caller's organisation certificate.
     *
     * @param csrPem the CSR in PEM format
     * @param clientId the IDP client ID of the calling organisation
     * @return a DTO containing the signed certificate and its chain
     */
    SignCertResponseDTO signAndRecord(String csrPem, String clientId);

    /**
     * Issue a short-lived bootstrap certificate package for the given organisation.
     * Generates a key pair, creates and signs a CSR, and packages the result as a
     * ZIP archive containing PKCS#12 keystore, truststore, and their passwords.
     *
     * @param clientId the IDP client ID of the calling organisation
     * @param commonName the common name (CN) to use as the certificate subject
     * @return a ZIP archive as a byte array
     */
    byte[] issueBootstrapPackage(String clientId, String commonName);
}
