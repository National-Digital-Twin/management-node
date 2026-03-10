/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.providers.certificate;

import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.dbt.ndtp.ia.node.management.exception.CertificateSigningException;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.OrganisationCertificateDTO;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.certificates.SignCertResponseDTO;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.CertificateEventType;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.CertificateType;
import uk.gov.dbt.ndtp.ia.node.management.service.data.CertificateEventService;
import uk.gov.dbt.ndtp.ia.node.management.service.data.OrganisationCertificateService;
import uk.gov.dbt.ndtp.ia.node.management.utils.cryptography.PemUtil;

/**
 * Implementation of {@link CertificateSigningProvider} that delegates to
 * {@link VaultPkiService} for signing and {@link CertificateEventService} for audit.
 */
@Service
@Slf4j
public class CertificateSigningProviderImpl implements CertificateSigningProvider {

    private final VaultPkiService vaultPkiService;
    private final OrganisationCertificateService certificateService;
    private final CertificateEventService eventService;

    public CertificateSigningProviderImpl(
            VaultPkiService vaultPkiService,
            OrganisationCertificateService certificateService,
            CertificateEventService eventService) {
        this.vaultPkiService = vaultPkiService;
        this.certificateService = certificateService;
        this.eventService = eventService;
    }

    @Override
    @Transactional
    public SignCertResponseDTO signAndRecord(String csrPem, String clientId) {
        OrganisationCertificateDTO cert = certificateService
                .findByClientId(clientId)
                .orElseThrow(() -> {
                    log.warn("No certificate record found for client {}", clientId);
                    return new CertificateSigningException("No certificate record found for client");
                });

        if (!Boolean.TRUE.equals(cert.getCertificateAutomationEnabled())) {
            log.warn("Certificate automation not enabled for client {}", clientId);
            throw new CertificateSigningException("Certificate automation is not enabled for this organisation");
        }

        if (!Boolean.TRUE.equals(cert.getIsRenewable())) {
            log.warn("Certificate is not marked as renewable for client {}", clientId);
            throw new CertificateSigningException("Certificate is not renewable");
        }

        cert.setRequestedAt(Timestamp.from(Instant.now()));

        SignCertResponseDTO response = vaultPkiService.signCsr(csrPem, Optional.empty(), Optional.empty());

        X509Certificate x509 = PemUtil.parseCertificate(response.getCertificate());
        cert.setSubjectDn(x509.getSubjectX500Principal().getName());
        cert.setSerialNumber(response.getSerialNumber());
        cert.setIssuedAt(Timestamp.from(x509.getNotBefore().toInstant()));
        cert.setExpiresAt(
                Timestamp.from(Instant.ofEpochSecond(response.getExpiration().longValue())));
        cert.setType(CertificateType.AUTOMATED);

        certificateService.save(cert);

        eventService.recordEvent(cert.getId(), CertificateType.AUTOMATED, CertificateEventType.RENEWED, clientId);

        log.info("Certificate signed and recorded for client {}, serial {}", clientId, response.getSerialNumber());

        return response;
    }
}
