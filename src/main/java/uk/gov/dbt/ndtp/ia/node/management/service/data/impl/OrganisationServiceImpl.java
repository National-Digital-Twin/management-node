package uk.gov.dbt.ndtp.ia.node.management.service.data.impl;

import org.springframework.stereotype.Service;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.OrganisationRepository;
import uk.gov.dbt.ndtp.ia.node.management.service.data.OrganisationService;

/**
 * Implementation of the OrganisationService interface.
 */
@Service
public class OrganisationServiceImpl implements OrganisationService {

    private final OrganisationRepository organisationRepository;

    /**
     * Constructor-based dependency injection.
     *
     * @param organisationRepository the organisation repository
     */
    public OrganisationServiceImpl(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }


}