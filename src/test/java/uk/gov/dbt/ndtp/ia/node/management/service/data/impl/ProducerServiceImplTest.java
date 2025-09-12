/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.data.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dbt.ndtp.ia.node.management.converter.impl.OrganisationProducerConverter;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProducerDTO;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Producer;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.ProducerRepository;

@ExtendWith(MockitoExtension.class)
class ProducerServiceImplTest {

    @Mock
    private ProducerRepository producerRepository;

    @Mock
    private OrganisationProducerConverter organisationProducerConverter;

    @InjectMocks
    private ProducerServiceImpl producerService;

    private Producer producer;
    private ProducerDTO producerDTO;
    private final Long producerId = 1L;
    private final String clientId = "test-client-id";

    @BeforeEach
    void setUp() {
        // Set up test data
        producer = new Producer();
        producer.setId(producerId);
        producer.setIdpClientId(clientId);
        producer.setName("Test Producer");
        producer.setActive(true);

        producerDTO = ProducerDTO.builder()
                .id(producerId)
                .idpClientId(clientId)
                .name("Test Producer")
                .active(true)
                .build();
    }

    @Test
    void getProducersByIds_withValidIds_shouldReturnProducerDTOs() {
        // Arrange
        List<Long> producerIds = List.of(producerId);
        List<Producer> producers = List.of(producer);
        List<ProducerDTO> producerDTOs = List.of(producerDTO);

        when(producerRepository.findByIds(producerIds)).thenReturn(producers);
        when(organisationProducerConverter.toDtoList(producers)).thenReturn(producerDTOs);

        // Act
        List<ProducerDTO> result = producerService.getProducersByIds(producerIds);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(producerId, result.get(0).getId());
        assertEquals(clientId, result.get(0).getIdpClientId());
        assertEquals("Test Producer", result.get(0).getName());
        assertEquals(true, result.get(0).getActive());

        // Verify
        verify(producerRepository).findByIds(producerIds);
        verify(organisationProducerConverter).toDtoList(producers);
    }

    @Test
    void getProducersByIds_withEmptyIds_shouldReturnEmptyList() {
        // Arrange
        List<Long> emptyIds = Collections.emptyList();
        List<Producer> emptyProducers = Collections.emptyList();
        List<ProducerDTO> emptyDTOs = Collections.emptyList();

        when(producerRepository.findByIds(emptyIds)).thenReturn(emptyProducers);
        when(organisationProducerConverter.toDtoList(emptyProducers)).thenReturn(emptyDTOs);

        // Act
        List<ProducerDTO> result = producerService.getProducersByIds(emptyIds);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify
        verify(producerRepository).findByIds(emptyIds);
        verify(organisationProducerConverter).toDtoList(emptyProducers);
    }

    @Test
    void getProducersByClientId_withValidClientId_shouldReturnProducerDTOs() {
        // Arrange
        List<Producer> producers = List.of(producer);
        List<ProducerDTO> producerDTOs = List.of(producerDTO);

        when(producerRepository.findByIdpClientId(clientId)).thenReturn(producers);
        when(organisationProducerConverter.toDtoList(producers)).thenReturn(producerDTOs);

        // Act
        List<ProducerDTO> result = producerService.getProducersByClientId(clientId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(producerId, result.get(0).getId());
        assertEquals(clientId, result.get(0).getIdpClientId());
        assertEquals("Test Producer", result.get(0).getName());
        assertEquals(true, result.get(0).getActive());

        // Verify
        verify(producerRepository).findByIdpClientId(clientId);
        verify(organisationProducerConverter).toDtoList(producers);
    }

    @Test
    void getProducersByClientId_withNonExistingClientId_shouldReturnEmptyList() {
        // Arrange
        String nonExistingClientId = "non-existing-client-id";
        List<Producer> emptyProducers = Collections.emptyList();
        List<ProducerDTO> emptyDTOs = Collections.emptyList();

        when(producerRepository.findByIdpClientId(nonExistingClientId)).thenReturn(emptyProducers);
        when(organisationProducerConverter.toDtoList(emptyProducers)).thenReturn(emptyDTOs);

        // Act
        List<ProducerDTO> result = producerService.getProducersByClientId(nonExistingClientId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify
        verify(producerRepository).findByIdpClientId(nonExistingClientId);
        verify(organisationProducerConverter).toDtoList(emptyProducers);
    }
}
