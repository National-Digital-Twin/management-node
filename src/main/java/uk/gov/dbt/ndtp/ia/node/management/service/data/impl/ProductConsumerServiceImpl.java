/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.data.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.dbt.ndtp.ia.node.management.converter.impl.ProductConsumerConverter;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProductConsumerDTO;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.ProductConsumer;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.ConsumerProviderRepository;
import uk.gov.dbt.ndtp.ia.node.management.service.data.ProductConsumerService;

/**
 * Implementation of the ConsumerAllowedDataProviderService interface.
 */
@Service
public class ProductConsumerServiceImpl implements ProductConsumerService {

    private final ConsumerProviderRepository consumerProviderRepository;
    private final ProductConsumerConverter consumerProviderConverter;

    /**
     * Constructor-based dependency injection.
     *
     * @param consumerProviderRepository the consumer allowed data provider repository
     * @param productConsumerConverter   the converter for entity-to-DTO conversion
     */
    public ProductConsumerServiceImpl(
            ConsumerProviderRepository consumerProviderRepository, ProductConsumerConverter productConsumerConverter) {
        this.consumerProviderRepository = consumerProviderRepository;
        this.consumerProviderConverter = productConsumerConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProductConsumerDTO> findByConsumerId(Long consumerId) {
        List<ProductConsumer> entities = consumerProviderRepository.findByConsumerId(consumerId);
        return consumerProviderConverter.toDtoList(entities);
    }

    @Override
    public List<ProductConsumerDTO> findByDataProviderId(Long providerId) {
        List<ProductConsumer> entities = consumerProviderRepository.findByProductId(providerId);
        return consumerProviderConverter.toDtoList(entities);
    }
}
