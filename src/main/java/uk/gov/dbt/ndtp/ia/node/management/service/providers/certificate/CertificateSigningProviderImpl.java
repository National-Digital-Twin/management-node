/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.providers.certificate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.dbt.ndtp.ia.node.management.exception.CertificateSigningException;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.OrganisationCertificateDTO;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.certificates.SignCertResponseDTO;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.CertificateEventType;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.CertificateType;
import uk.gov.dbt.ndtp.ia.node.management.service.data.CertificateEventService;
import uk.gov.dbt.ndtp.ia.node.management.service.data.OrganisationCertificateService;
import uk.gov.dbt.ndtp.ia.node.management.utils.cryptography.KeyStoreBuilder;
import uk.gov.dbt.ndtp.ia.node.management.utils.cryptography.PemUtil;

/**
 * Implementation of {@link CertificateSigningProvider} that delegates to
 * {@link VaultPkiService} for signing and {@link CertificateEventService} for audit.
 */
@Service
@Slf4j
public class CertificateSigningProviderImpl implements CertificateSigningProvider {

    private static final String BOOTSTRAP_ALIAS = "bootstrap";
    private static final int PASSWORD_BYTES = 24;

    private final VaultPkiService vaultPkiService;
    private final OrganisationCertificateService certificateService;
    private final CertificateEventService eventService;
    private final String bootstrapTtl;

    public CertificateSigningProviderImpl(
            VaultPkiService vaultPkiService,
            OrganisationCertificateService certificateService,
            CertificateEventService eventService,
            @Value("${application.bootstrap.ttl:2h}") String bootstrapTtl) {
        this.vaultPkiService = vaultPkiService;
        this.certificateService = certificateService;
        this.eventService = eventService;
        this.bootstrapTtl = bootstrapTtl;
    }

    @Override
    @Transactional
    public SignCertResponseDTO signAndRecord(String csrPem, String clientId) {
        OrganisationCertificateDTO cert = lookupCertificate(clientId);

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

        updateCertificateRecord(cert, response, CertificateType.AUTOMATED);
        certificateService.save(cert);
        eventService.recordEvent(cert.getId(), CertificateType.AUTOMATED, CertificateEventType.RENEWED, clientId);

        log.info("Certificate signed and recorded for client {}, serial {}", clientId, response.getSerialNumber());

        return response;
    }

    @Override
    @Transactional
    public byte[] issueBootstrapPackage(String clientId, String commonName) {
        OrganisationCertificateDTO cert = lookupCertificate(clientId);

        if (cert.getType() == CertificateType.AUTOMATED) {
            log.warn("Overwriting active automated certificate for client {}", clientId);
        }

        KeyPair keyPair = vaultPkiService.generateKeyPair();
        String csrPem = vaultPkiService.createCsr(keyPair, commonName);
        SignCertResponseDTO signResponse = vaultPkiService.signCsr(csrPem, Optional.empty(), Optional.of(bootstrapTtl));

        X509Certificate signedCert = PemUtil.parseCertificate(signResponse.getCertificate());
        List<X509Certificate> caChain = parseCaChain(signResponse.getCaChain());

        String password = generatePassword();
        byte[] keystoreBytes =
                KeyStoreBuilder.createKeyStore(keyPair.getPrivate(), signedCert, caChain, password, BOOTSTRAP_ALIAS);
        byte[] truststoreBytes = KeyStoreBuilder.createTrustStore(caChain, password);

        byte[] zipBytes = assembleZip(keystoreBytes, truststoreBytes, password);

        cert.setRequestedAt(Timestamp.from(Instant.now()));
        updateCertificateRecord(cert, signResponse, CertificateType.BOOTSTRAP);
        cert.setIsRenewable(true);
        certificateService.save(cert);
        eventService.recordEvent(cert.getId(), CertificateType.BOOTSTRAP, CertificateEventType.ISSUED, clientId);

        log.info("Bootstrap certificate issued for client {}, serial {}", clientId, signResponse.getSerialNumber());

        return zipBytes;
    }

    private OrganisationCertificateDTO lookupCertificate(String clientId) {
        return certificateService.findByClientId(clientId).orElseThrow(() -> {
            log.warn("No certificate record found for client {}", clientId);
            return new CertificateSigningException("No certificate record found for client");
        });
    }

    private void updateCertificateRecord(
            OrganisationCertificateDTO cert, SignCertResponseDTO response, CertificateType type) {
        X509Certificate x509 = PemUtil.parseCertificate(response.getCertificate());
        cert.setSubjectDn(x509.getSubjectX500Principal().getName());
        cert.setSerialNumber(response.getSerialNumber());
        cert.setIssuedAt(Timestamp.from(x509.getNotBefore().toInstant()));
        cert.setExpiresAt(
                Timestamp.from(Instant.ofEpochSecond(response.getExpiration().longValue())));
        cert.setType(type);
    }

    private List<X509Certificate> parseCaChain(List<String> caChainPems) {
        if (caChainPems == null) {
            return List.of();
        }
        return caChainPems.stream().map(PemUtil::parseCertificate).toList();
    }

    private static String generatePassword() {
        byte[] bytes = new byte[PASSWORD_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] assembleZip(byte[] keystoreBytes, byte[] truststoreBytes, String password) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos)) {
            addZipEntry(zos, "keystore.p12", keystoreBytes);
            addZipEntry(zos, "truststore.p12", truststoreBytes);
            addZipEntry(zos, "keystore.password", password.getBytes(StandardCharsets.UTF_8));
            addZipEntry(zos, "truststore.password", password.getBytes(StandardCharsets.UTF_8));
            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new CertificateSigningException("Failed to assemble bootstrap certificate package", e);
        }
    }

    private static void addZipEntry(ZipOutputStream zos, String name, byte[] data) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(data);
        zos.closeEntry();
    }
}
