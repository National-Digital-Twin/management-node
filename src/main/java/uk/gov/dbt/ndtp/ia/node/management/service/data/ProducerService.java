/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.data;

import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProducerDTO;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Producer;

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
    List<ProducerDTO> getProducersByConsumerIds(List<Long> producerIds);

    List<ProducerDTO> getProducersByClientId(String clientId);

    /**
     * Retrieves producers for a client, additionally constrained by a compiled caller filter.
     *
     * @param clientId the IDP client ID to scope by
     * @param filter an additional predicate, AND-ed with the client scoping; {@code null} for none
     * @return producers matching both the client scope and the filter
     */
    List<ProducerDTO> getProducersByClientId(String clientId, Specification<Producer> filter);
}
