package uk.gov.dbt.ndtp.ia.node.management.service.data;

import uk.gov.dbt.ndtp.ia.node.management.model.dto.ConsumerDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for managing ConsumerId entities.
 */
public interface ConsumerService {
    /**
     * Find a ConsumerId by its ID.
     *
     * @param id the IDP client ID to search for
     * @return a list of ConsumerIdDTO objects matching the ID
     */
    Optional<ConsumerDTO> findById(Long id);



    /**
     * Find an ConsumerId by its IDP client ID.
     *
     * @param idpClientId the IDP client ID to search for
     * @return a list of ConsumerIdDTO objects matching the IDP client ID
     */
    List<ConsumerDTO> findByIdpClientId(String idpClientId);


    /**
     * Retrieves a map of consumers identified by their client_id
     *
     * @param providers a list of provider IDs for which associated consumers need to be retrieved
     * @return a map where the keys are provider IDs and the values are lists of ConsumerDTO objects
     *         representing the consumers associated with each provider
     */
    Map<String, List<ConsumerDTO>> getConsumersOfProviders(List<Long> providers);
}