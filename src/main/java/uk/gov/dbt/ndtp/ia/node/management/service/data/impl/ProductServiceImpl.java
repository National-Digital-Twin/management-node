package uk.gov.dbt.ndtp.ia.node.management.service.data.impl;

import org.springframework.stereotype.Service;
import uk.gov.dbt.ndtp.ia.node.management.converter.impl.ProductConverter;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProductDTO;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Product;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.ProductRepository;
import uk.gov.dbt.ndtp.ia.node.management.service.data.ProductService;

import java.util.List;
import java.util.Optional;

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
     * @param productConverter the converter for entity-to-DTO conversion
     */
    public ProductServiceImpl(
            ProductRepository productRepository,
            ProductConverter productConverter) {
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
        List<Product> dataProviders =
            productRepository.findByIds(ids);
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