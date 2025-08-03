package uk.gov.dbt.ndtp.ia.node.management.service.data;

import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProductDTO;

import java.util.List;

/**
 * Service interface for managing OrganisationDataProvider entities.
 */
public interface ProductService {
    
    /**
     * Retrieves a list of OrganisationDataProviderDTO objects by their IDs.
     *
     * @param ids the list of IDs to search for
     * @return a list of OrganisationDataProviderDTO objects
     */
    List<ProductDTO> getProductsByIds(List<Long> ids);


    /**
     * Retrieves a list of DataProviderDTO objects associated with the specified producer IDs.
     *
     * @param ProducerIds the list of producer IDs for which data providers need to be retrieved
     * @return a list of DataProviderDTO objects corresponding to the given producer IDs
     */
    List<ProductDTO> getProductsByProducerIds(List<Long> ProducerIds);
}