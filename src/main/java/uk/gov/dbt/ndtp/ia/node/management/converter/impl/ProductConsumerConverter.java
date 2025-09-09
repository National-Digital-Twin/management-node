/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.converter.impl;

import org.springframework.stereotype.Component;
import uk.gov.dbt.ndtp.ia.node.management.converter.EntityDtoConverter;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProductConsumerDTO;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.ProductConsumer;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.ProductConsumerId;

/**
 * Converter for ConsumerAllowedDataProvider entity and ConsumerAllowedDataProviderDTO.
 */
@Component
public class ProductConsumerConverter implements EntityDtoConverter<ProductConsumer, ProductConsumerDTO> {

    /**
     * Converts a ConsumerAllowedDataProvider entity to a ConsumerAllowedDataProviderDTO.
     *
     * @param entity the entity to convert
     * @return the converted DTO
     */
    @Override
    public ProductConsumerDTO toDto(ProductConsumer entity) {
        if (entity == null) {
            return null;
        }

        return ProductConsumerDTO.builder()
                .productId(entity.getId().getProductId())
                .consumerId(entity.getId().getConsumerId())
                .grantedTs(entity.getGrantedTs())
                .validity(entity.getValidity())
                .build();
    }

    /**
     * Converts a ConsumerAllowedDataProviderDTO to a ConsumerAllowedDataProvider entity.
     *
     * @param dto the DTO to convert
     * @return the converted entity
     */
    @Override
    public ProductConsumer toEntity(ProductConsumerDTO dto) {
        if (dto == null) {
            return null;
        }

        ProductConsumer entity = new ProductConsumer();

        // Create and set the embedded ID
        ProductConsumerId id = new ProductConsumerId();
        id.setProductId(dto.getProductId());
        id.setConsumerId(dto.getConsumerId());
        entity.setId(id);

        entity.setGrantedTs(dto.getGrantedTs());
        entity.setValidity(dto.getValidity());

        return entity;
    }
}
