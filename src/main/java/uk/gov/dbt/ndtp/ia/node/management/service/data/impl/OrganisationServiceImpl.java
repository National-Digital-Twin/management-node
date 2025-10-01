/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

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
