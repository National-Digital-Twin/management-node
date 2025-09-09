/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.data.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.dbt.ndtp.ia.node.management.converter.impl.ProductConverter;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProductDTO;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Product;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.ProductRepository;
import uk.gov.dbt.ndtp.ia.node.management.service.data.ProductService;

/**
 * Implementation of the OrganisationDataProviderService interface.
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductConverter productConverter;

    /**
     * Constructor-based dependency injection.
     *
     * @param productRepository the organisation data provider repository
     * @param productConverter  the converter for entity-to-DTO conversion
     */
    public ProductServiceImpl(ProductRepository productRepository, ProductConverter productConverter) {
        this.productRepository = productRepository;
        this.productConverter = productConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProductDTO> getProductsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Product> dataProviders = productRepository.findByIds(ids);
        return Optional.ofNullable(dataProviders)
                .map(productConverter::toDtoList)
                .orElse(List.of());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProductDTO> getProductsByProducerIds(List<Long> producerIds) {
        List<Product> dataProviders = productRepository.findByProducerIds(producerIds);
        return Optional.ofNullable(dataProviders)
                .map(productConverter::toDtoList)
                .orElse(List.of());
    }
}
