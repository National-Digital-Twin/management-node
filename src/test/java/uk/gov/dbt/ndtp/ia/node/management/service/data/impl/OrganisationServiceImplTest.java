/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.data.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.OrganisationRepository;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceImplTest {

    @Mock
    private OrganisationRepository organisationRepository;

    @InjectMocks
    private OrganisationServiceImpl organisationService;

    @BeforeEach
    void setUp() {
        // No setup needed as the service has no methods to test yet
    }

    @Test
    void organisationService_shouldBeInitialized() {
        // This test verifies that the service is properly initialized with its dependencies
        assertNotNull(organisationService);
        assertNotNull(organisationRepository);
    }
}
