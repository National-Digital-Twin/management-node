/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.providers.configuration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.*;
import uk.gov.dbt.ndtp.ia.node.management.service.data.ConsumerService;
import uk.gov.dbt.ndtp.ia.node.management.service.data.ProducerService;
import uk.gov.dbt.ndtp.ia.node.management.service.data.ProductConsumerService;
import uk.gov.dbt.ndtp.ia.node.management.service.data.ProductService;

@ExtendWith(MockitoExtension.class)
class ConfigurationProviderImplTest {

    @Mock
    private ConsumerService consumerService;

    @Mock
    private ProductConsumerService consumerAllowedDataProvidersService;

    @Mock
    private ProductService dataProviderService;

    @Mock
    private ProducerService producerService;

    @InjectMocks
    private ConfigurationProviderImpl configurationProvider;

    private final String clientId = "test-client-id";
    private final Long consumerId = 1L;
    private final Long producerId = 2L;
    private final Long productId = 3L;

    private ConsumerDTO consumerDTO;
    private ProducerDTO producerDTO;
    private ProductDTO productDTO;
    private ProductConsumerDTO productConsumerDTO;

    @BeforeEach
    void setUp() {
        // Set up consumer
        consumerDTO = ConsumerDTO.builder()
                .id(consumerId)
                .name("Test Consumer")
                .idpClientId(clientId)
                .build();

        // Set up producer
        producerDTO = ProducerDTO.builder()
                .id(producerId)
                .name("Test Producer")
                .idpClientId(clientId)
                .active(true)
                .build();

        // Set up product
        productDTO = ProductDTO.builder()
                .id(productId)
                .name("Test Product")
                .producerId(producerId)
                .consumers(new ArrayList<>())
                .build();

        // Set up product consumer relationship
        productConsumerDTO = ProductConsumerDTO.builder()
                .consumerId(consumerId)
                .productId(productId)
                .validity(null) // No validity constraint
                .build();
    }

    // Tests for getConsumerConfigByClientId

    @Test
    void getConsumerConfigByClientId_withValidClientIdAndNoConsumerId_shouldReturnConfig() {
        // Arrange
        List<ConsumerDTO> consumers = List.of(consumerDTO);
        List<ProductConsumerDTO> productConsumers = List.of(productConsumerDTO);
        List<ProductDTO> products = List.of(productDTO);
        List<ProducerDTO> producers = List.of(producerDTO);

        when(consumerService.findByIdpClientId(clientId)).thenReturn(consumers);
        when(consumerAllowedDataProvidersService.findByConsumerId(consumerId)).thenReturn(productConsumers);
        when(dataProviderService.getProductsByIds(List.of(productId))).thenReturn(products);
        when(producerService.getProducersByIds(List.of(producerId))).thenReturn(producers);

        // Act
        ConsumerConfigDTO result = configurationProvider.getConsumerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals(1, result.getProducers().size());
        assertEquals(producerId, result.getProducers().get(0).getId());

        // Verify
        verify(consumerService).findByIdpClientId(clientId);
        verify(consumerAllowedDataProvidersService).findByConsumerId(consumerId);
        verify(dataProviderService).getProductsByIds(List.of(productId));
        verify(producerService).getProducersByIds(List.of(producerId));
    }

    @Test
    void getConsumerConfigByClientId_withValidClientIdAndConsumerId_shouldReturnFilteredConfig() {
        // Arrange
        List<ConsumerDTO> allConsumers = List.of(consumerDTO);
        List<ProductConsumerDTO> productConsumers = List.of(productConsumerDTO);
        List<ProductDTO> products = List.of(productDTO);
        List<ProducerDTO> producers = List.of(producerDTO);

        when(consumerService.findByIdpClientId(clientId)).thenReturn(allConsumers);
        when(consumerAllowedDataProvidersService.findByConsumerId(consumerId)).thenReturn(productConsumers);
        when(dataProviderService.getProductsByIds(List.of(productId))).thenReturn(products);
        when(producerService.getProducersByIds(List.of(producerId))).thenReturn(producers);

        // Act
        ConsumerConfigDTO result = configurationProvider.getConsumerConfigByClientId(clientId, Optional.of(consumerId));

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals(1, result.getProducers().size());
        assertEquals(producerId, result.getProducers().get(0).getId());

        // Verify
        verify(consumerService).findByIdpClientId(clientId);
        verify(consumerAllowedDataProvidersService).findByConsumerId(consumerId);
        verify(dataProviderService).getProductsByIds(List.of(productId));
        verify(producerService).getProducersByIds(List.of(producerId));
    }

    @Test
    void getConsumerConfigByClientId_withNoMatchingConsumers_shouldReturnEmptyConfig() {
        // Arrange
        when(consumerService.findByIdpClientId(clientId)).thenReturn(Collections.emptyList());
        when(dataProviderService.getProductsByIds(Collections.emptyList())).thenReturn(Collections.emptyList());
        when(producerService.getProducersByIds(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        ConsumerConfigDTO result = configurationProvider.getConsumerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertTrue(result.getProducers().isEmpty());

        // Verify
        verify(consumerService).findByIdpClientId(clientId);
        verify(consumerAllowedDataProvidersService, never()).findByConsumerId(any());
        verify(dataProviderService).getProductsByIds(Collections.emptyList());
        verify(producerService).getProducersByIds(Collections.emptyList());
    }

    @Test
    void getConsumerConfigByClientId_withNoMatchingConsumerForSpecificId_shouldReturnEmptyConfig() {
        // Arrange
        ConsumerDTO differentConsumer =
                ConsumerDTO.builder().id(999L).idpClientId(clientId).build();

        when(consumerService.findByIdpClientId(clientId)).thenReturn(List.of(differentConsumer));
        when(dataProviderService.getProductsByIds(Collections.emptyList())).thenReturn(Collections.emptyList());
        when(producerService.getProducersByIds(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        ConsumerConfigDTO result = configurationProvider.getConsumerConfigByClientId(clientId, Optional.of(consumerId));

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertTrue(result.getProducers().isEmpty());

        // Verify
        verify(consumerService).findByIdpClientId(clientId);
        verify(consumerAllowedDataProvidersService, never()).findByConsumerId(any());
        verify(dataProviderService).getProductsByIds(Collections.emptyList());
        verify(producerService).getProducersByIds(Collections.emptyList());
    }

    @Test
    void getConsumerConfigByClientId_withNoValidDataProviders_shouldReturnEmptyConfig() {
        // Arrange
        List<ConsumerDTO> consumers = List.of(consumerDTO);

        when(consumerService.findByIdpClientId(clientId)).thenReturn(consumers);
        when(consumerAllowedDataProvidersService.findByConsumerId(consumerId)).thenReturn(Collections.emptyList());
        when(dataProviderService.getProductsByIds(Collections.emptyList())).thenReturn(Collections.emptyList());
        when(producerService.getProducersByIds(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        ConsumerConfigDTO result = configurationProvider.getConsumerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertTrue(result.getProducers().isEmpty());

        // Verify
        verify(consumerService).findByIdpClientId(clientId);
        verify(consumerAllowedDataProvidersService).findByConsumerId(consumerId);
        verify(dataProviderService).getProductsByIds(Collections.emptyList());
        verify(producerService).getProducersByIds(Collections.emptyList());
    }

    @Test
    void getConsumerConfigByClientId_withExpiredValidity_shouldReturnEmptyConfig() {
        // Arrange
        List<ConsumerDTO> consumers = List.of(consumerDTO);

        // Create expired product consumer relationship
        ProductConsumerDTO expiredProductConsumer = ProductConsumerDTO.builder()
                .consumerId(consumerId)
                .productId(productId)
                .validity(BigDecimal.valueOf(30)) // 30 days validity
                .grantedTs(Timestamp.from(Instant.now().minus(60, ChronoUnit.DAYS))) // 60 days ago
                .build();

        when(consumerService.findByIdpClientId(clientId)).thenReturn(consumers);
        when(consumerAllowedDataProvidersService.findByConsumerId(consumerId))
                .thenReturn(List.of(expiredProductConsumer));
        when(dataProviderService.getProductsByIds(Collections.emptyList())).thenReturn(Collections.emptyList());
        when(producerService.getProducersByIds(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        ConsumerConfigDTO result = configurationProvider.getConsumerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertTrue(result.getProducers().isEmpty());

        // Verify
        verify(consumerService).findByIdpClientId(clientId);
        verify(consumerAllowedDataProvidersService).findByConsumerId(consumerId);
        verify(dataProviderService).getProductsByIds(Collections.emptyList());
        verify(producerService).getProducersByIds(Collections.emptyList());
    }

    @Test
    void getConsumerConfigByClientId_withValidityButNoGrantedTs_shouldReturnEmptyConfig() {
        // Arrange
        List<ConsumerDTO> consumers = List.of(consumerDTO);

        // Create product consumer relationship with validity but no grantedTs
        ProductConsumerDTO invalidProductConsumer = ProductConsumerDTO.builder()
                .consumerId(consumerId)
                .productId(productId)
                .validity(BigDecimal.valueOf(30)) // 30 days validity
                .grantedTs(null) // No granted timestamp
                .build();

        when(consumerService.findByIdpClientId(clientId)).thenReturn(consumers);
        when(consumerAllowedDataProvidersService.findByConsumerId(consumerId))
                .thenReturn(List.of(invalidProductConsumer));
        when(dataProviderService.getProductsByIds(Collections.emptyList())).thenReturn(Collections.emptyList());
        when(producerService.getProducersByIds(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        ConsumerConfigDTO result = configurationProvider.getConsumerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertTrue(result.getProducers().isEmpty());

        // Verify
        verify(consumerService).findByIdpClientId(clientId);
        verify(consumerAllowedDataProvidersService).findByConsumerId(consumerId);
        verify(dataProviderService).getProductsByIds(Collections.emptyList());
        verify(producerService).getProducersByIds(Collections.emptyList());
    }

    @Test
    void getConsumerConfigByClientId_withValidityZero_shouldReturnConfig() {
        // Arrange
        List<ConsumerDTO> consumers = List.of(consumerDTO);

        // Create product consumer relationship with zero validity
        ProductConsumerDTO zeroValidityProductConsumer = ProductConsumerDTO.builder()
                .consumerId(consumerId)
                .productId(productId)
                .validity(BigDecimal.ZERO) // Zero validity means no expiration
                .build();

        List<ProductDTO> products = List.of(productDTO);
        List<ProducerDTO> producers = List.of(producerDTO);

        when(consumerService.findByIdpClientId(clientId)).thenReturn(consumers);
        when(consumerAllowedDataProvidersService.findByConsumerId(consumerId))
                .thenReturn(List.of(zeroValidityProductConsumer));
        when(dataProviderService.getProductsByIds(List.of(productId))).thenReturn(products);
        when(producerService.getProducersByIds(List.of(producerId))).thenReturn(producers);

        // Act
        ConsumerConfigDTO result = configurationProvider.getConsumerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals(1, result.getProducers().size());

        // Verify
        verify(consumerService).findByIdpClientId(clientId);
        verify(consumerAllowedDataProvidersService).findByConsumerId(consumerId);
        verify(dataProviderService).getProductsByIds(List.of(productId));
        verify(producerService).getProducersByIds(List.of(producerId));
    }

    @Test
    void getConsumerConfigByClientId_withNoActiveProducers_shouldReturnEmptyConfig() {
        // Arrange
        List<ConsumerDTO> consumers = List.of(consumerDTO);
        List<ProductConsumerDTO> productConsumers = List.of(productConsumerDTO);
        List<ProductDTO> products = List.of(productDTO);

        // Create inactive producer
        ProducerDTO inactiveProducer =
                ProducerDTO.builder().id(producerId).active(false).build();

        when(consumerService.findByIdpClientId(clientId)).thenReturn(consumers);
        when(consumerAllowedDataProvidersService.findByConsumerId(consumerId)).thenReturn(productConsumers);
        when(dataProviderService.getProductsByIds(List.of(productId))).thenReturn(products);
        when(producerService.getProducersByIds(List.of(producerId))).thenReturn(List.of(inactiveProducer));

        // Act
        ConsumerConfigDTO result = configurationProvider.getConsumerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertTrue(result.getProducers().isEmpty());

        // Verify
        verify(consumerService).findByIdpClientId(clientId);
        verify(consumerAllowedDataProvidersService).findByConsumerId(consumerId);
        verify(dataProviderService).getProductsByIds(List.of(productId));
        verify(producerService).getProducersByIds(List.of(producerId));
    }

    // Tests for getProducerConfigByClientId

    @Test
    void getProducerConfigByClientId_withValidClientIdAndNoProducerId_shouldReturnConfig() {
        // Arrange
        List<ProducerDTO> producers = List.of(producerDTO);
        // Add product to producer's dataProviders list
        producerDTO.getDataProviders().add(productDTO);

        Map<String, List<ConsumerDTO>> consumersMap = new HashMap<>();
        consumersMap.put(productId.toString(), List.of(consumerDTO));

        when(producerService.getProducersByClientId(clientId)).thenReturn(producers);
        when(consumerService.getConsumersOfProviders(List.of(productId))).thenReturn(consumersMap);
        when(consumerAllowedDataProvidersService.findByDataProviderId(productId))
                .thenReturn(List.of(productConsumerDTO));
        when(consumerService.findById(consumerId)).thenReturn(Optional.of(consumerDTO));

        // Act
        ProducerConfigDTO result = configurationProvider.getProducerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals(1, result.getProducers().size());
        assertEquals(producerId, result.getProducers().get(0).getId());
        assertEquals(1, result.getProducers().get(0).getDataProviders().size());

        // Verify
        verify(producerService).getProducersByClientId(clientId);
        verify(consumerService).getConsumersOfProviders(List.of(productId));
        verify(consumerAllowedDataProvidersService).findByDataProviderId(productId);
        verify(consumerService).findById(consumerId);
    }

    @Test
    void getProducerConfigByClientId_withValidClientIdAndProducerId_shouldReturnFilteredConfig() {
        // Arrange
        List<ProducerDTO> allProducers = List.of(producerDTO);
        // Add product to producer's dataProviders list
        producerDTO.getDataProviders().add(productDTO);

        Map<String, List<ConsumerDTO>> consumersMap = new HashMap<>();
        consumersMap.put(productId.toString(), List.of(consumerDTO));

        when(producerService.getProducersByClientId(clientId)).thenReturn(allProducers);
        when(consumerService.getConsumersOfProviders(List.of(productId))).thenReturn(consumersMap);
        when(consumerAllowedDataProvidersService.findByDataProviderId(productId))
                .thenReturn(List.of(productConsumerDTO));
        when(consumerService.findById(consumerId)).thenReturn(Optional.of(consumerDTO));

        // Act
        ProducerConfigDTO result = configurationProvider.getProducerConfigByClientId(clientId, Optional.of(producerId));

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals(1, result.getProducers().size());
        assertEquals(producerId, result.getProducers().get(0).getId());

        // Verify
        verify(producerService).getProducersByClientId(clientId);
        verify(consumerService).getConsumersOfProviders(List.of(productId));
        verify(consumerAllowedDataProvidersService).findByDataProviderId(productId);
        verify(consumerService).findById(consumerId);
    }

    @Test
    void getProducerConfigByClientId_withNoMatchingProducers_shouldReturnEmptyConfig() {
        // Arrange
        when(producerService.getProducersByClientId(clientId)).thenReturn(Collections.emptyList());
        when(consumerService.getConsumersOfProviders(Collections.emptyList())).thenReturn(Collections.emptyMap());

        // Act
        ProducerConfigDTO result = configurationProvider.getProducerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertTrue(result.getProducers().isEmpty());

        // Verify
        verify(producerService).getProducersByClientId(clientId);
        verify(consumerService).getConsumersOfProviders(Collections.emptyList());
    }

    @Test
    void getProducerConfigByClientId_withNoMatchingProducerForSpecificId_shouldReturnEmptyConfig() {
        // Arrange
        ProducerDTO differentProducer = ProducerDTO.builder()
                .id(999L)
                .idpClientId(clientId)
                .active(true)
                .build();

        when(producerService.getProducersByClientId(clientId)).thenReturn(List.of(differentProducer));
        when(consumerService.getConsumersOfProviders(Collections.emptyList())).thenReturn(Collections.emptyMap());

        // Act
        ProducerConfigDTO result = configurationProvider.getProducerConfigByClientId(clientId, Optional.of(producerId));

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertTrue(result.getProducers().isEmpty());

        // Verify
        verify(producerService).getProducersByClientId(clientId);
        verify(consumerService).getConsumersOfProviders(Collections.emptyList());
    }

    @Test
    void getProducerConfigByClientId_withNoActiveProducers_shouldReturnEmptyConfig() {
        // Arrange
        ProducerDTO inactiveProducer = ProducerDTO.builder()
                .id(producerId)
                .idpClientId(clientId)
                .active(false)
                .build();

        when(producerService.getProducersByClientId(clientId)).thenReturn(List.of(inactiveProducer));
        when(consumerService.getConsumersOfProviders(Collections.emptyList())).thenReturn(Collections.emptyMap());

        // Act
        ProducerConfigDTO result = configurationProvider.getProducerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertTrue(result.getProducers().isEmpty());

        // Verify
        verify(producerService).getProducersByClientId(clientId);
        verify(consumerService).getConsumersOfProviders(Collections.emptyList());
    }

    @Test
    void getProducerConfigByClientId_withNullConsumers_shouldInitializeConsumersList() {
        // Arrange
        List<ProducerDTO> producers = List.of(producerDTO);
        // Add product to producer's dataProviders list with null consumers
        productDTO.setConsumers(null); // Null consumers list
        producerDTO.getDataProviders().add(productDTO);

        Map<String, List<ConsumerDTO>> consumersMap = new HashMap<>();
        consumersMap.put(productId.toString(), List.of(consumerDTO));

        when(producerService.getProducersByClientId(clientId)).thenReturn(producers);
        when(consumerService.getConsumersOfProviders(List.of(productId))).thenReturn(consumersMap);
        when(consumerAllowedDataProvidersService.findByDataProviderId(productId))
                .thenReturn(List.of(productConsumerDTO));
        when(consumerService.findById(consumerId)).thenReturn(Optional.of(consumerDTO));

        // Act
        ProducerConfigDTO result = configurationProvider.getProducerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals(1, result.getProducers().size());
        assertNotNull(result.getProducers().get(0).getDataProviders().get(0).getConsumers());

        // Verify
        verify(producerService).getProducersByClientId(clientId);
        verify(consumerService).getConsumersOfProviders(List.of(productId));
        verify(consumerAllowedDataProvidersService).findByDataProviderId(productId);
        verify(consumerService).findById(consumerId);
    }

    @Test
    void getProducerConfigByClientId_withExpiredValidity_shouldNotAddConsumer() {
        // Arrange
        List<ProducerDTO> producers = List.of(producerDTO);
        // Add product to producer's dataProviders list
        producerDTO.getDataProviders().add(productDTO);

        // Create expired product consumer relationship
        ProductConsumerDTO expiredProductConsumer = ProductConsumerDTO.builder()
                .consumerId(consumerId)
                .productId(productId)
                .validity(BigDecimal.valueOf(30)) // 30 days validity
                .grantedTs(Timestamp.from(Instant.now().minus(60, ChronoUnit.DAYS))) // 60 days ago
                .build();

        Map<String, List<ConsumerDTO>> consumersMap = new HashMap<>();
        consumersMap.put(productId.toString(), List.of(consumerDTO));

        when(producerService.getProducersByClientId(clientId)).thenReturn(producers);
        when(consumerService.getConsumersOfProviders(List.of(productId))).thenReturn(consumersMap);
        when(consumerAllowedDataProvidersService.findByDataProviderId(productId))
                .thenReturn(List.of(expiredProductConsumer));

        // Act
        ProducerConfigDTO result = configurationProvider.getProducerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals(1, result.getProducers().size());
        assertTrue(result.getProducers()
                .get(0)
                .getDataProviders()
                .get(0)
                .getConsumers()
                .isEmpty());

        // Verify
        verify(producerService).getProducersByClientId(clientId);
        verify(consumerService).getConsumersOfProviders(List.of(productId));
        verify(consumerAllowedDataProvidersService).findByDataProviderId(productId);
        verify(consumerService, never()).findById(any());
    }

    @Test
    void getProducerConfigByClientId_withValidityButNoGrantedTs_shouldNotAddConsumer() {
        // Arrange
        List<ProducerDTO> producers = List.of(producerDTO);
        // Add product to producer's dataProviders list
        producerDTO.getDataProviders().add(productDTO);

        // Create product consumer relationship with validity but no grantedTs
        ProductConsumerDTO invalidProductConsumer = ProductConsumerDTO.builder()
                .consumerId(consumerId)
                .productId(productId)
                .validity(BigDecimal.valueOf(30)) // 30 days validity
                .grantedTs(null) // No granted timestamp
                .build();

        Map<String, List<ConsumerDTO>> consumersMap = new HashMap<>();
        consumersMap.put(productId.toString(), List.of(consumerDTO));

        when(producerService.getProducersByClientId(clientId)).thenReturn(producers);
        when(consumerService.getConsumersOfProviders(List.of(productId))).thenReturn(consumersMap);
        when(consumerAllowedDataProvidersService.findByDataProviderId(productId))
                .thenReturn(List.of(invalidProductConsumer));

        // Act
        ProducerConfigDTO result = configurationProvider.getProducerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals(1, result.getProducers().size());
        assertTrue(result.getProducers()
                .get(0)
                .getDataProviders()
                .get(0)
                .getConsumers()
                .isEmpty());

        // Verify
        verify(producerService).getProducersByClientId(clientId);
        verify(consumerService).getConsumersOfProviders(List.of(productId));
        verify(consumerAllowedDataProvidersService).findByDataProviderId(productId);
        verify(consumerService, never()).findById(any());
    }

    @Test
    void getProducerConfigByClientId_withConsumerNotFound_shouldNotAddConsumer() {
        // Arrange
        List<ProducerDTO> producers = List.of(producerDTO);
        // Add product to producer's dataProviders list
        producerDTO.getDataProviders().add(productDTO);

        Map<String, List<ConsumerDTO>> consumersMap = new HashMap<>();
        consumersMap.put(productId.toString(), List.of(consumerDTO));

        when(producerService.getProducersByClientId(clientId)).thenReturn(producers);
        when(consumerService.getConsumersOfProviders(List.of(productId))).thenReturn(consumersMap);
        when(consumerAllowedDataProvidersService.findByDataProviderId(productId))
                .thenReturn(List.of(productConsumerDTO));
        when(consumerService.findById(consumerId)).thenReturn(Optional.empty());

        // Act
        ProducerConfigDTO result = configurationProvider.getProducerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getClientId());
        assertEquals(1, result.getProducers().size());
        assertTrue(result.getProducers()
                .get(0)
                .getDataProviders()
                .get(0)
                .getConsumers()
                .isEmpty());

        // Verify
        verify(producerService).getProducersByClientId(clientId);
        verify(consumerService).getConsumersOfProviders(List.of(productId));
        verify(consumerAllowedDataProvidersService).findByDataProviderId(productId);
        verify(consumerService).findById(consumerId);
    }

    // Tests for isValidProvider method through public methods

    @Test
    void isValidProvider_withValidityNullShouldBeValid() {
        // Arrange
        List<ConsumerDTO> consumers = List.of(consumerDTO);
        ProductConsumerDTO validProductConsumer = ProductConsumerDTO.builder()
                .consumerId(consumerId)
                .productId(productId)
                .validity(null) // Null validity means no expiration
                .build();

        List<ProductDTO> products = List.of(productDTO);
        List<ProducerDTO> producers = List.of(producerDTO);

        when(consumerService.findByIdpClientId(clientId)).thenReturn(consumers);
        when(consumerAllowedDataProvidersService.findByConsumerId(consumerId))
                .thenReturn(List.of(validProductConsumer));
        when(dataProviderService.getProductsByIds(List.of(productId))).thenReturn(products);
        when(producerService.getProducersByIds(List.of(producerId))).thenReturn(producers);

        // Act
        ConsumerConfigDTO result = configurationProvider.getConsumerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProducers().size());
    }

    @Test
    void isValidProvider_withValidGrantedTsAndValidity_shouldBeValid() {
        // Arrange
        List<ConsumerDTO> consumers = List.of(consumerDTO);
        ProductConsumerDTO validProductConsumer = ProductConsumerDTO.builder()
                .consumerId(consumerId)
                .productId(productId)
                .validity(BigDecimal.valueOf(30)) // 30 days validity
                .grantedTs(Timestamp.from(Instant.now().minus(15, ChronoUnit.DAYS))) // 15 days ago, still valid
                .build();

        List<ProductDTO> products = List.of(productDTO);
        List<ProducerDTO> producers = List.of(producerDTO);

        when(consumerService.findByIdpClientId(clientId)).thenReturn(consumers);
        when(consumerAllowedDataProvidersService.findByConsumerId(consumerId))
                .thenReturn(List.of(validProductConsumer));
        when(dataProviderService.getProductsByIds(List.of(productId))).thenReturn(products);
        when(producerService.getProducersByIds(List.of(producerId))).thenReturn(producers);

        // Act
        ConsumerConfigDTO result = configurationProvider.getConsumerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProducers().size());
    }

    // Test for isValidGrantedTs method through isValidProvider

    @Test
    void isValidGrantedTs_withFutureDate_shouldBeValid() {
        // Arrange
        List<ConsumerDTO> consumers = List.of(consumerDTO);
        ProductConsumerDTO validProductConsumer = ProductConsumerDTO.builder()
                .consumerId(consumerId)
                .productId(productId)
                .validity(BigDecimal.valueOf(30)) // 30 days validity
                .grantedTs(Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS))) // Future date
                .build();

        List<ProductDTO> products = List.of(productDTO);
        List<ProducerDTO> producers = List.of(producerDTO);

        when(consumerService.findByIdpClientId(clientId)).thenReturn(consumers);
        when(consumerAllowedDataProvidersService.findByConsumerId(consumerId))
                .thenReturn(List.of(validProductConsumer));
        when(dataProviderService.getProductsByIds(List.of(productId))).thenReturn(products);
        when(producerService.getProducersByIds(List.of(producerId))).thenReturn(producers);

        // Act
        ConsumerConfigDTO result = configurationProvider.getConsumerConfigByClientId(clientId, Optional.empty());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProducers().size());
    }
}
