/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.converter.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ConsumerDTO;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Consumer;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Organisation;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.OrganisationRepository;

@ExtendWith(MockitoExtension.class)
class ConsumerConverterTest {

    @Mock
    private OrganisationRepository organisationRepository;

    @InjectMocks
    private ConsumerConverter converter;

    private Consumer entity;
    private ConsumerDTO dto;
    private Organisation organisation;

    private final Long consumerId = 1L;
    private final String consumerName = "Test Consumer";
    private final String idpClientId = "test-client-id";
    private final Long orgId = 101L;
    private final String orgName = "Test Organisation";

    @BeforeEach
    void setUp() {
        // Create test organisation
        organisation = new Organisation();
        organisation.setId(orgId);
        organisation.setName(orgName);

        // Create test entity
        entity = new Consumer();
        entity.setId(consumerId);
        entity.setName(consumerName);
        entity.setIdpClientId(idpClientId);
        entity.setOrg(organisation);

        // Create test DTO
        dto = new ConsumerDTO();
        dto.setId(consumerId);
        dto.setName(consumerName);
        dto.setIdpClientId(idpClientId);
        dto.setOrgId(orgId);
    }

    @Test
    void toDto_withNullEntity_shouldReturnNull() {
        // Act
        ConsumerDTO result = converter.toDto(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toDto_withValidEntity_shouldReturnCorrectDTO() {
        // Act
        ConsumerDTO result = converter.toDto(entity);

        // Assert
        assertNotNull(result);
        assertEquals(consumerId, result.getId());
        assertEquals(consumerName, result.getName());
        assertEquals(idpClientId, result.getIdpClientId());
        assertEquals(orgId, result.getOrgId());
    }

    @Test
    void toDto_withNullOrg_shouldReturnDTOWithNullOrgId() {
        // Arrange
        entity.setOrg(null);

        // Act
        ConsumerDTO result = converter.toDto(entity);

        // Assert
        assertNotNull(result);
        assertEquals(consumerId, result.getId());
        assertEquals(consumerName, result.getName());
        assertEquals(idpClientId, result.getIdpClientId());
        assertNull(result.getOrgId());
    }

    @Test
    void toEntity_withNullDTO_shouldReturnNull() {
        // Act
        Consumer result = converter.toEntity(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toEntity_withValidDTO_shouldReturnCorrectEntity() {
        // Arrange
        when(organisationRepository.findById(orgId)).thenReturn(Optional.of(organisation));

        // Act
        Consumer result = converter.toEntity(dto);

        // Assert
        assertNotNull(result);
        assertEquals(consumerId, result.getId());
        assertEquals(consumerName, result.getName());
        assertEquals(idpClientId, result.getIdpClientId());
        assertNotNull(result.getOrg());
        assertEquals(orgId, result.getOrg().getId());
        assertEquals(orgName, result.getOrg().getName());

        // Verify
        verify(organisationRepository, times(1)).findById(orgId);
    }

    @Test
    void toEntity_withNullOrgId_shouldReturnEntityWithNullOrg() {
        // Arrange
        dto.setOrgId(null);

        // Act
        Consumer result = converter.toEntity(dto);

        // Assert
        assertNotNull(result);
        assertEquals(consumerId, result.getId());
        assertEquals(consumerName, result.getName());
        assertEquals(idpClientId, result.getIdpClientId());
        assertNull(result.getOrg());

        // Verify
        verify(organisationRepository, never()).findById(any());
    }

    @Test
    void toEntity_withNonExistentOrgId_shouldReturnEntityWithNullOrg() {
        // Arrange
        when(organisationRepository.findById(orgId)).thenReturn(Optional.empty());

        // Act
        Consumer result = converter.toEntity(dto);

        // Assert
        assertNotNull(result);
        assertEquals(consumerId, result.getId());
        assertEquals(consumerName, result.getName());
        assertEquals(idpClientId, result.getIdpClientId());
        assertNull(result.getOrg());

        // Verify
        verify(organisationRepository, times(1)).findById(orgId);
    }
}
