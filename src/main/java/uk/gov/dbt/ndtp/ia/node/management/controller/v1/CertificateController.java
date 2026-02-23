/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
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
public class CertificateController {

    private final VaultPkiService pkiService;

    public CertificateController(VaultPkiService pkiService) {
        this.pkiService = pkiService;
    }

    /**
     * Creates RSA key pair using configured PKI service.
     *
     * @return a DTO containing the generated public and private keys in PEM format
     */
    @GetMapping("/keyPair")
    @Operation(
            summary = "Create RSA key pair",
            description = "Creates a new RSA 2048-bit key pair using the configured PKI service.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponse(
            responseCode = "200",
            description = "RSA key pair created successfully",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateKeyResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public CreateKeyResponseDTO createKeyPair() {
        CreateKeyRequestDTO rsaRequest =
                CreateKeyRequestDTO.builder().keySize(2048).algorithm("RSA").build();
        return pkiService.createKeyPair(rsaRequest.getAlgorithm(), rsaRequest.getKeySize());
    }

    /**
     * Creates a Certificate Signing Request (CSR) from provided public and private keys.
     *
     * @param req the CSR request DTO containing keys and subject details
     * @return a DTO containing the generated CSR PEM
     */
    @PostMapping("/csr/create")
    @Operation(
            summary = "Create Certificate Signing Request (CSR)",
            description = "Generates a CSR from the provided public and private keys and subject information.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponse(
            responseCode = "200",
            description = "CSR created successfully",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateCsrResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "500", description = "Internal server error")
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
    @Operation(
            summary = "Sign CSR",
            description = "Signs the provided CSR using the PKI service. Uses the provided role or default if not specified. Uses default TTL if not specified.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponse(
            responseCode = "200",
            description = "CSR signed successfully",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignCertResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid CSR or parameters")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public SignCertResponseDTO signCsr(@RequestBody SignCertRequestDTO req) {
        return pkiService.signCsr(req.getCsr(), Optional.empty(), Optional.empty());
    }

    /**
     * Retrieves the intermediate certificate and its chain.
     *
     * @return a DTO containing the PEM certificate, CA chain, and parsed info
     */
    @GetMapping("/intermediate")
    @Operation(
            summary = "Get intermediate certificate",
            description = "Retrieves the configured intermediate certificate and its CA chain.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponse(
            responseCode = "200",
            description = "Intermediate certificate retrieved successfully",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = IntermediateCertResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public IntermediateCertResponseDTO getIntermediateCertificate() {
        return pkiService.getIntermediateCertificate();
    }
}
