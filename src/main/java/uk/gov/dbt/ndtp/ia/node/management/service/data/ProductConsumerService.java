package uk.gov.dbt.ndtp.ia.node.management.service.data;

import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProductConsumerDTO;

import java.util.List;

/**
 * Service interface for managing ConsumerAllowedDataProvider entities.
 */
public interface ProductConsumerService {
    
    /**
     * Find all ConsumerAllowedDataProvider entities by consumer ID.
     *
     * @param consumerId the consumer ID
     * @return a list of ConsumerAllowedDataProviderDTO objects
     */
    List<ProductConsumerDTO> findByConsumerId(Long consumerId);

    List<ProductConsumerDTO> findByDataProviderId(Long providerId);

}