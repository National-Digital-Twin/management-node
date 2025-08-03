package uk.gov.dbt.ndtp.ia.node.management.service.data;

import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProducerDTO;

import java.util.List;

/**
 * Service interface for managing OrganisationProducer entities.
 */
public interface ProducerService {
    
    /**
     * Retrieves a map of organisation IDs to lists of producer DTOs associated with those organisations.
     *
     * @param producerIds the list of producer IDs to retrieve
     * @return a map where keys are organisation IDs and values are lists of producer DTOs associated with each organisation
     */
     List<ProducerDTO> getProducersByIds(List<Long> producerIds);


    List<ProducerDTO> getProducersByClientId(String clientId);

}