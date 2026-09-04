/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterNode;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterRequestParser;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ConsumerConfigDTO;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProducerConfigDTO;
import uk.gov.dbt.ndtp.ia.node.management.model.jwt.EnhancedPrincipal;
import uk.gov.dbt.ndtp.ia.node.management.service.providers.configuration.ConfigurationProvider;

@RestController
@RequestMapping("/api/v1/configuration")
@Slf4j
@Tag(name = "Configuration", description = "Endpoints for retrieving Federators Producer and Consumer configuration.")
public class ConfigurationController {

    private final ConfigurationProvider configurationProvider;
    private final FilterRequestParser filterRequestParser;

    public ConfigurationController(
            ConfigurationProvider configurationProvider, FilterRequestParser filterRequestParser) {
        this.configurationProvider = configurationProvider;
        this.filterRequestParser = filterRequestParser;
    }

    @GetMapping("/producer")
    @PreAuthorize("hasAuthority('ROLE_management-node:access_producer_configurations')")
    @Operation(
            summary = "Get Federator Producer configuration",
            description =
                    "Returns configuration for the authenticated client, optionally scoped to a specific producer.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponse(
            responseCode = "200",
            description = "Federator Producer configuration returned",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProducerConfigDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ProducerConfigDTO getProducerConfigurations(
            @Parameter(hidden = true) @AuthenticationPrincipal EnhancedPrincipal principal,
            @Parameter(name = "producer_id", description = "Optional Producer identifier to filter configuration")
                    @RequestParam(value = "producer_id", required = false)
                    Long producerId,
            @Parameter(
                            name = "filter",
                            description =
                                    "Optional JSON-encoded filter (FilterNode: a Comparison or a Group of them) narrowing"
                                            + " the returned producers, evaluated by the database alongside producer_id")
                    @RequestParam(value = "filter", required = false)
                    String filter) {
        log.info("Preparing Federator Producer Config for producer {}", producerId);
        Optional<FilterNode> filterNode = filterRequestParser.parse(filter);
        return configurationProvider.getProducerConfigByClientId(
                principal.clientId(), producerId != null ? Optional.of(producerId) : Optional.empty(), filterNode);
    }

    @GetMapping("/consumer")
    @PreAuthorize("hasAuthority('ROLE_management-node:access_consumer_configurations')")
    @Operation(
            summary = "Get Federator Consumer configuration",
            description =
                    "Returns configuration for the authenticated client, optionally scoped to a specific consumer.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponse(
            responseCode = "200",
            description = "Consumer configuration returned",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ConsumerConfigDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ConsumerConfigDTO getConsumerConfigurations(
            @Parameter(hidden = true) @AuthenticationPrincipal EnhancedPrincipal principal,
            @Parameter(name = "consumer_id", description = "Optional Consumer identifier to filter configuration")
                    @RequestParam(value = "consumer_id", required = false)
                    Long consumerId,
            @Parameter(
                            name = "filter",
                            description =
                                    "Optional JSON-encoded filter (FilterNode: a Comparison or a Group of them) narrowing"
                                            + " the returned consumers, evaluated by the database alongside consumer_id")
                    @RequestParam(value = "filter", required = false)
                    String filter) {
        log.info("Preparing Consumer Config for client Id {} and Consumer {}", principal.clientId(), consumerId);

        Optional<FilterNode> filterNode = filterRequestParser.parse(filter);
        return configurationProvider.getConsumerConfigByClientId(
                principal.clientId(), consumerId != null ? Optional.of(consumerId) : Optional.empty(), filterNode);
    }
}
