/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.converter.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProductConsumerDTO;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.ProductConsumer;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.ProductConsumerId;

@ExtendWith(MockitoExtension.class)
class ProductConsumerConverterTest {

    @InjectMocks
    private ProductConsumerConverter converter;

    private ProductConsumer entity;
    private ProductConsumerDTO dto;
    private final Long consumerId = 1L;
    private final Long dataProviderId = 101L;
    private final Timestamp grantedTs = Timestamp.from(Instant.now());
    private final BigDecimal validity = new BigDecimal("365");

    @BeforeEach
    void setUp() {
        // Create test entity
        entity = new ProductConsumer();
        ProductConsumerId id = new ProductConsumerId();
        id.setConsumerId(consumerId);
        id.setProductId(dataProviderId);
        entity.setId(id);
        entity.setGrantedTs(grantedTs);
        entity.setValidity(validity);

        // Create test DTO
        dto = new ProductConsumerDTO();
        dto.setConsumerId(consumerId);
        dto.setProductId(dataProviderId);
        dto.setGrantedTs(grantedTs);
        dto.setValidity(validity);
    }

    @Test
    void toDto_withNullEntity_shouldReturnNull() {
        // Act
        ProductConsumerDTO result = converter.toDto(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toDto_withValidEntity_shouldReturnCorrectDTO() {
        // Act
        ProductConsumerDTO result = converter.toDto(entity);

        // Assert
        assertNotNull(result);
        assertEquals(consumerId, result.getConsumerId());
        assertEquals(dataProviderId, result.getProductId());
        assertEquals(grantedTs, result.getGrantedTs());
        assertEquals(validity, result.getValidity());
    }

    @Test
    void toEntity_withNullDTO_shouldReturnNull() {
        // Act
        ProductConsumer result = converter.toEntity(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toEntity_withValidDTO_shouldReturnCorrectEntity() {
        // Act
        ProductConsumer result = converter.toEntity(dto);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(consumerId, result.getId().getConsumerId());
        assertEquals(dataProviderId, result.getId().getProductId());
        assertEquals(grantedTs, result.getGrantedTs());
        assertEquals(validity, result.getValidity());
    }
}
