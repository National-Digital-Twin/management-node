/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.controller.v1;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.certificates.CertificateInfoDTO;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.certificates.CreateCsrRequestDTO;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.certificates.CreateCsrResponseDTO;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.certificates.CreateKeyResponseDTO;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.certificates.IntermediateCertResponseDTO;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.certificates.SignCertResponseDTO;
import uk.gov.dbt.ndtp.ia.node.management.service.providers.certificate.VaultPkiService;

@ExtendWith(MockitoExtension.class)
class CertificateControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VaultPkiService pkiService;

    @InjectMocks
    private CertificateController certificateController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(certificateController).build();
    }

    @Test
    void createKeyPair_shouldReturnKeyPair() throws Exception {
        CreateKeyResponseDTO response = CreateKeyResponseDTO.builder()
                .algorithm("RSA")
                .publicKeyPem("PUB")
                .privateKeyPem("PRIV")
                .build();
        when(pkiService.createKeyPair(anyString(), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/certificate/keyPair").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithm").value("RSA"));
    }

    @Test
    void createCsr_shouldReturnCsr() throws Exception {
        CreateCsrResponseDTO response = new CreateCsrResponseDTO("id", "CSR_PEM");
        when(pkiService.createCsr(any(CreateCsrRequestDTO.class))).thenReturn(response);

        String jsonRequest = "{\"commonName\":\"test\"}";

        mockMvc.perform(post("/api/v1/certificate/csr/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.csrPem").value("CSR_PEM"));
    }

    @Test
    void signCsr_shouldReturnSignedCert() throws Exception {
        SignCertResponseDTO response = SignCertResponseDTO.builder()
                .certificate("CERT")
                .serialNumber("123")
                .build();
        when(pkiService.signCsr(anyString(), any(), any())).thenReturn(response);

        String jsonRequest = "{\"csr\":\"CSR\"}";

        mockMvc.perform(post("/api/v1/certificate/csr/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificate").value("CERT"));
    }

    @Test
    void getIntermediateCertificate_shouldReturnCertificateWithInfo() throws Exception {
        String mockCert = "-----BEGIN CERTIFICATE-----\nABC\n-----END CERTIFICATE-----";
        CertificateInfoDTO info = CertificateInfoDTO.builder()
                .subject("CN=Test")
                .issuer("CN=Root")
                .serialNumber("12345")
                .build();
        IntermediateCertResponseDTO responseDTO = IntermediateCertResponseDTO.builder()
                .certificate(mockCert)
                .caChain(null)
                .info(info)
                .build();
        when(pkiService.getIntermediateCertificate()).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/certificate/intermediate").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.certificate").value(mockCert))
                .andExpect(jsonPath("$.info.subject").value("CN=Test"))
                .andExpect(jsonPath("$.info.issuer").value("CN=Root"))
                .andExpect(jsonPath("$.info.serialNumber").value("12345"));
    }
}
