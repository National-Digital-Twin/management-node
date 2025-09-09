/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.controller.v1;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ConsumerConfigDTO;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProducerConfigDTO;
import uk.gov.dbt.ndtp.ia.node.management.model.jwt.EnhancedPrincipal;
import uk.gov.dbt.ndtp.ia.node.management.service.providers.configuration.ConfigurationProvider;

@RestController
@RequestMapping("/api/v1/configuration")
@Slf4j
public class ConfigurationController {

    private final ConfigurationProvider configurationProvider;

    public ConfigurationController(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    @GetMapping("/producer")
    @PreAuthorize("hasRole('ROLE_management-node:access_producer_configurations')")
    public ProducerConfigDTO getProducerConfigurations(
            @AuthenticationPrincipal EnhancedPrincipal principal,
            @RequestParam(value = "producer_id", required = false) Long producer_id) {
        log.info("Preparing Producer Config for producer {}", producer_id);
        return configurationProvider.getProducerConfigByClientId(
                principal.clientId(), producer_id != null ? Optional.of(producer_id) : Optional.empty());
    }

    @GetMapping("/consumer")
    @PreAuthorize("hasRole('ROLE_management-node:access_consumer_configurations')")
    public ConsumerConfigDTO getConsumerConfigurations(
            @AuthenticationPrincipal EnhancedPrincipal principal,
            @RequestParam(value = "consumer_id", required = false) Long consumerId) {
        log.info("Preparing Consumer Config for client Id {} and Consumer {}", principal.clientId(), consumerId);

        return configurationProvider.getConsumerConfigByClientId(
                principal.clientId(), consumerId != null ? Optional.of(consumerId) : Optional.empty());
    }
}
