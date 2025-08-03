package uk.gov.dbt.ndtp.ia.node.management.service.data.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.OrganisationRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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