package uk.gov.dbt.ndtp.ia.node.management.service.data.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dbt.ndtp.ia.node.management.converter.impl.ProductConsumerConverter;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProductConsumerDTO;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.ProductConsumer;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.ProductConsumerId;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.ConsumerProviderRepository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductConsumerServiceImplTest {

    @Mock
    private ConsumerProviderRepository consumerProviderRepository;

    @Mock
    private ProductConsumerConverter productConsumerConverter;

    @InjectMocks
    private ProductConsumerServiceImpl productConsumerService;

    private ProductConsumer productConsumer;
    private ProductConsumerDTO productConsumerDTO;
    private final Long consumerId = 1L;
    private final Long productId = 2L;

    @BeforeEach
    void setUp() {
        // Set up test data
        ProductConsumerId id = new ProductConsumerId();
        id.setConsumerId(consumerId);
        id.setProductId(productId);

        productConsumer = new ProductConsumer();
        productConsumer.setId(id);
        productConsumer.setGrantedTs(Timestamp.from(Instant.now()));
        productConsumer.setValidity(BigDecimal.ZERO);

        productConsumerDTO = ProductConsumerDTO.builder()
                .consumerId(consumerId)
                .productId(productId)
                .grantedTs(Timestamp.from(Instant.now()))
                .validity(BigDecimal.ZERO)
                .build();
    }

    @Test
    void findByConsumerId_withValidId_shouldReturnProductConsumerDTOs() {
        // Arrange
        List<ProductConsumer> productConsumers = List.of(productConsumer);
        List<ProductConsumerDTO> productConsumerDTOs = List.of(productConsumerDTO);

        when(consumerProviderRepository.findByConsumerId(consumerId)).thenReturn(productConsumers);
        when(productConsumerConverter.toDtoList(productConsumers)).thenReturn(productConsumerDTOs);

        // Act
        List<ProductConsumerDTO> result = productConsumerService.findByConsumerId(consumerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(consumerId, result.get(0).getConsumerId());
        assertEquals(productId, result.get(0).getProductId());

        // Verify
        verify(consumerProviderRepository).findByConsumerId(consumerId);
        verify(productConsumerConverter).toDtoList(productConsumers);
    }

    @Test
    void findByConsumerId_withNonExistingId_shouldReturnEmptyList() {
        // Arrange
        Long nonExistingId = 999L;
        List<ProductConsumer> emptyList = Collections.emptyList();
        List<ProductConsumerDTO> emptyDTOList = Collections.emptyList();

        when(consumerProviderRepository.findByConsumerId(nonExistingId)).thenReturn(emptyList);
        when(productConsumerConverter.toDtoList(emptyList)).thenReturn(emptyDTOList);

        // Act
        List<ProductConsumerDTO> result = productConsumerService.findByConsumerId(nonExistingId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify
        verify(consumerProviderRepository).findByConsumerId(nonExistingId);
        verify(productConsumerConverter).toDtoList(emptyList);
    }

    @Test
    void findByDataProviderId_withValidId_shouldReturnProductConsumerDTOs() {
        // Arrange
        List<ProductConsumer> productConsumers = List.of(productConsumer);
        List<ProductConsumerDTO> productConsumerDTOs = List.of(productConsumerDTO);

        when(consumerProviderRepository.findByProductId(productId)).thenReturn(productConsumers);
        when(productConsumerConverter.toDtoList(productConsumers)).thenReturn(productConsumerDTOs);

        // Act
        List<ProductConsumerDTO> result = productConsumerService.findByDataProviderId(productId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(consumerId, result.get(0).getConsumerId());
        assertEquals(productId, result.get(0).getProductId());

        // Verify
        verify(consumerProviderRepository).findByProductId(productId);
        verify(productConsumerConverter).toDtoList(productConsumers);
    }

    @Test
    void findByDataProviderId_withNonExistingId_shouldReturnEmptyList() {
        // Arrange
        Long nonExistingId = 999L;
        List<ProductConsumer> emptyList = Collections.emptyList();
        List<ProductConsumerDTO> emptyDTOList = Collections.emptyList();

        when(consumerProviderRepository.findByProductId(nonExistingId)).thenReturn(emptyList);
        when(productConsumerConverter.toDtoList(emptyList)).thenReturn(emptyDTOList);

        // Act
        List<ProductConsumerDTO> result = productConsumerService.findByDataProviderId(nonExistingId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify
        verify(consumerProviderRepository).findByProductId(nonExistingId);
        verify(productConsumerConverter).toDtoList(emptyList);
    }
}