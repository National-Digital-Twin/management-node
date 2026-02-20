/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.controller.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.certificates.*;
import uk.gov.dbt.ndtp.ia.node.management.service.providers.certificate.VaultPkiService;

/**
 * Controller for certificate and PKI management.
 * Provides endpoints for creating key pairs, CSRs, and signing certificates.
 */
@RestController
@RequestMapping("/api/v1/certificate")
@Slf4j
@Tag(name = "Certificate", description = "Endpoints for certificate management.")
@RequiredArgsConstructor
public class CertificateController {

    private final VaultPkiService pkiService;


    /**
     * Creates RSA key pair using configured PKI service.
     *
     * @return a DTO containing the generated public and private keys in PEM format
     */
    @GetMapping("/keyPair")
    public CreateKeyResponseDTO createKeyPair() {
        CreateKeyRequestDTO rsaRequest = CreateKeyRequestDTO.builder().keySize(2048).algorithm("RSA").build();
        return pkiService.createKeyPair(rsaRequest.getAlgorithm(), rsaRequest.getKeySize());
    }

    /**
     * Creates a Certificate Signing Request (CSR) from provided public and private keys.
     *
     * @param req the CSR request DTO containing keys and subject details
     * @return a DTO containing the generated CSR PEM
     */
    @PostMapping("/csr/create")
    public CreateCsrResponseDTO createCsr(@RequestBody CreateCsrRequestDTO req) {
        return pkiService.createCsr(req);
    }

    /**
     * Signs a CSR using the default role and TTL.
     *
     * @param req the sign request DTO containing the CSR PEM
     * @return a DTO containing the signed certificate and its chain
     */
    @PostMapping("/csr/sign")
    public SignCertResponseDTO signCsr(@RequestBody SignCertRequestDTO req) {
        return pkiService.signCsr(req.getCsr(), "default-role","24h");
    }

    /**
     * Retrieves the intermediate certificate and its chain.
     *
     * @return a DTO containing the PEM certificate, CA chain, and parsed info
     */
    @GetMapping("/intermediate")
    public IntermediateCertResponseDTO getIntermediateCertificate() {
        return pkiService.getIntermediateCertificate();
    }
}
