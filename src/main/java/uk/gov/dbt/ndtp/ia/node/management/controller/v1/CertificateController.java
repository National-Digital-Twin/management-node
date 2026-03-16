/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uk.gov.dbt.ndtp.ia.node.management.config.AllowBootstrapCertificates;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.certificates.*;
import uk.gov.dbt.ndtp.ia.node.management.model.jwt.EnhancedPrincipal;
import uk.gov.dbt.ndtp.ia.node.management.service.providers.certificate.CertificateSigningProvider;
import uk.gov.dbt.ndtp.ia.node.management.service.providers.certificate.VaultPkiService;

/**
 * Controller for certificate and PKI management.
 * Provides endpoints for creating key pairs, CSRs, and signing certificates.
 */
@RestController
@RequestMapping("/api/v1/certificate")
@AllowBootstrapCertificates
@Slf4j
@Tag(name = "Certificate", description = "Endpoints for certificate management.")
public class CertificateController {

    private final VaultPkiService pkiService;
    private final CertificateSigningProvider signingProvider;

    public CertificateController(VaultPkiService pkiService, CertificateSigningProvider signingProvider) {
        this.pkiService = pkiService;
        this.signingProvider = signingProvider;
    }

    /**
     * Creates RSA key pair using configured PKI service.
     *
     * @return a DTO containing the generated public and private keys in PEM format
     */
    @GetMapping("/keyPair")
    @PreAuthorize("hasAuthority('ROLE_management-node:create_keys')")
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
    @PreAuthorize("hasAuthority('ROLE_management-node:create_keys')")
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
     * Signs a CSR and records the result against the caller's organisation.
     *
     * @param req the sign request DTO containing the CSR PEM
     * @param principal the authenticated caller's identity
     * @return a DTO containing the signed certificate and its chain
     */
    @PostMapping("/csr/sign")
    @PreAuthorize("hasAuthority('ROLE_management-node:sign_certificate')")
    @Operation(
            summary = "Sign CSR",
            description =
                    "Signs the provided CSR using the PKI service. Uses the provided role or default if not specified. Uses default TTL if not specified.",
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
    public SignCertResponseDTO signCsr(
            @RequestBody SignCertRequestDTO req,
            @Parameter(hidden = true) @AuthenticationPrincipal EnhancedPrincipal principal) {
        return signingProvider.signAndRecord(req.getCsr(), principal.clientId());
    }

    /**
     * Retrieves the intermediate certificate and its chain.
     *
     * @return a DTO containing the PEM certificate, CA chain, and parsed info
     */
    @GetMapping("/intermediate")
    @PreAuthorize("hasAuthority('ROLE_management-node:access_public_certificates')")
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
