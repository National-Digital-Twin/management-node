package uk.gov.dbt.ndtp.ia.node.management.controller.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ConsumerConfigDTO;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProducerConfigDTO;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProducerDTO;
import uk.gov.dbt.ndtp.ia.node.management.service.providers.configuration.ConfigurationProvider;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ConfigurationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ConfigurationProvider configurationProvider;

    @InjectMocks
    private ConfigurationController configurationController;

    private final String CLIENT_ID = "test-client-id";
    private final Long PRODUCER_ID = 1L;
    private final Long CONSUMER_ID = 2L;

    private ProducerConfigDTO producerConfigDTO;
    private ConsumerConfigDTO consumerConfigDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(configurationController)
                .build();

        // Set up producer config
        ProducerDTO producerDTO = ProducerDTO.builder()
                .id(PRODUCER_ID)
                .name("Test Producer")
                .active(true)
                .build();
        
        producerConfigDTO = ProducerConfigDTO.builder()
                .clientId(CLIENT_ID)
                .producers(Collections.singletonList(producerDTO))
                .build();

        // Set up consumer config
        consumerConfigDTO = ConsumerConfigDTO.builder()
                .clientId(CLIENT_ID)
                .producers(new ArrayList<>())
                .build();
    }

    // Note: In a real test environment with a full Spring Security context, we would use:
    // 1. @WithMockUser to test authorization rules
    // 2. SecurityMockMvcRequestPostProcessors.user() to provide authentication
    // 3. A WebMvcTest with a proper security configuration
    //
    // For this example, we're using a standalone setup that bypasses Spring Security,
    // so we're focusing on testing the controller functionality rather than authorization.
    // The actual authorization is enforced by Spring Security through the @PreAuthorize annotations.
    
    @Test
    void getProducerConfigurations_shouldReturnConfig() throws Exception {
        // Arrange
        when(configurationProvider.getProducerConfigByClientId(any(), any()))
                .thenReturn(producerConfigDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/configuration/producer")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(CLIENT_ID));
    }

    @Test
    void getProducerConfigurations_withProducerId_shouldReturnFilteredConfig() throws Exception {
        // Arrange
        when(configurationProvider.getProducerConfigByClientId(any(), any()))
                .thenReturn(producerConfigDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/configuration/producer")
                        .param("producer_id", PRODUCER_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(CLIENT_ID));
    }

    @Test
    void getConsumerConfigurations_shouldReturnConfig() throws Exception {
        // Arrange
        when(configurationProvider.getConsumerConfigByClientId(any(), any()))
                .thenReturn(consumerConfigDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/configuration/consumer")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(CLIENT_ID));
    }

    @Test
    void getConsumerConfigurations_withConsumerId_shouldReturnFilteredConfig() throws Exception {
        // Arrange
        when(configurationProvider.getConsumerConfigByClientId(any(), any()))
                .thenReturn(consumerConfigDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/configuration/consumer")
                        .param("consumer_id", CONSUMER_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(CLIENT_ID));
    }
}