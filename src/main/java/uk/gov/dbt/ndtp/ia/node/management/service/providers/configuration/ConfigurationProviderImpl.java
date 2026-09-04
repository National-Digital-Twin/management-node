/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.providers.configuration;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterNode;
import uk.gov.dbt.ndtp.ia.node.management.filter.compiler.SpecificationPredicateCompiler;
import uk.gov.dbt.ndtp.ia.node.management.filter.registry.ResourceType;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.*;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Consumer;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Producer;
import uk.gov.dbt.ndtp.ia.node.management.service.data.ConsumerService;
import uk.gov.dbt.ndtp.ia.node.management.service.data.ProducerService;
import uk.gov.dbt.ndtp.ia.node.management.service.data.ProductConsumerService;
import uk.gov.dbt.ndtp.ia.node.management.service.providers.certificate.CertificateValidationProvider;

/**
 * Implementation of {@link ConfigurationProvider} that retrieves configuration from database services.
 */
@Service
public class ConfigurationProviderImpl implements ConfigurationProvider {

    private final ConsumerService consumerService;

    private final ProductConsumerService productConsumerService;

    private final ProducerService producerService;

    private final CertificateValidationProvider certificateValidationProvider;

    private final SpecificationPredicateCompiler specificationPredicateCompiler;

    /**
     * Constructs a new ConfigurationProviderImpl with required services.
     *
     * @param consumerService the consumer service
     * @param consumerAllowedDataProviders the product consumer service
     * @param producerService the producer service
     * @param certificateValidationProvider the certificate validation provider
     * @param specificationPredicateCompiler compiles a caller filter into a database predicate
     */
    public ConfigurationProviderImpl(
            ConsumerService consumerService,
            ProductConsumerService consumerAllowedDataProviders,
            ProducerService producerService,
            CertificateValidationProvider certificateValidationProvider,
            SpecificationPredicateCompiler specificationPredicateCompiler) {

        this.consumerService = consumerService;
        this.productConsumerService = consumerAllowedDataProviders;
        this.producerService = producerService;
        this.certificateValidationProvider = certificateValidationProvider;
        this.specificationPredicateCompiler = specificationPredicateCompiler;
    }

    /**
     * Checks if a granted timestamp is still valid based on the validity period.
     *
     * @param grantedTs the timestamp when access was granted
     * @param validity the validity period in days
     * @return true if valid, false otherwise
     */
    private static boolean isValidGrantedTs(Timestamp grantedTs, BigDecimal validity) {
        return grantedTs != null
                && grantedTs
                        .toInstant()
                        .plus(Duration.ofDays(validity.longValue()))
                        .isAfter(Instant.now());
    }

    @Override
    public ConsumerConfigDTO getConsumerConfigByClientId(String clientId, Optional<Long> consumerId) {
        return getConsumerConfigByClientId(clientId, consumerId, Optional.empty());
    }

    @Override
    public ConsumerConfigDTO getConsumerConfigByClientId(
            String clientId, Optional<Long> consumerId, Optional<FilterNode> filter) {
        List<ConsumerDTO> consumers = getFilteredConsumers(clientId, consumerId, filter);
        List<Long> consumerIds = consumers.stream().map(ConsumerDTO::getId).toList();

        List<ProductConsumerDTO> validProductConsumers = getValidProductConsumers(consumers);
        List<Long> validProductIds = validProductConsumers.stream()
                .map(ProductConsumerDTO::getProductId)
                .toList();

        List<ProducerDTO> producers = producerService.getProducersByConsumerIds(consumerIds).stream()
                .filter(ProducerDTO::getActive)
                .toList();

        producers = filterProducersByActiveCertificate(producers);

        // Filter products of each producer to only those in validProductIds
        if (!validProductIds.isEmpty()) {
            Set<Long> validIdsSet = new HashSet<>(validProductIds);
            producers.forEach(
                    p -> p.getProducts().removeIf(prod -> prod.getId() == null || !validIdsSet.contains(prod.getId())));
        } else {
            // If no valid products, clear products for all producers
            producers.forEach(p -> p.getProducts().clear());
        }

        // finding the products and adding the configurations
        producers.forEach(producer -> producer.getProducts().forEach(product -> {
            List<ProductConsumerDTO> configs = validProductConsumers.stream()
                    .filter(pc -> pc.getProductId().equals(product.getId()))
                    .toList();
            product.setConfigurations(configs);
        }));
        if (consumers.isEmpty()) {
            return ConsumerConfigDTO.builder()
                    .clientId(clientId)
                    .producers(List.of())
                    .build();
        }
        ConsumerDTO firstConsumer = consumers.getFirst();
        return ConsumerConfigDTO.builder()
                .scheduleExpression(firstConsumer.getScheduleExpression())
                .scheduleType(firstConsumer.getScheduleType())
                .clientId(clientId)
                .name(firstConsumer.getName())
                .producers(producers)
                .build();
    }

    /**
     * Retrieves valid product consumers for a list of consumers.
     *
     * @param consumers the list of consumers
     * @return a list of valid product consumer DTOs
     */
    private List<ProductConsumerDTO> getValidProductConsumers(List<ConsumerDTO> consumers) {
        List<ProductConsumerDTO> validProductIds = new ArrayList<>();

        consumers.forEach(consumer -> {
            List<ProductConsumerDTO> list = productConsumerService.findByConsumerId(consumer.getId()).stream()
                    .filter(this::isValidProvider)
                    .toList();

            validProductIds.addAll(list);
        });
        return validProductIds;
    }

    @Override
    public ProducerConfigDTO getProducerConfigByClientId(String clientId, Optional<Long> producerId) {
        return getProducerConfigByClientId(clientId, producerId, Optional.empty());
    }

    @Override
    public ProducerConfigDTO getProducerConfigByClientId(
            String clientId, Optional<Long> producerId, Optional<FilterNode> filter) {
        List<ProducerDTO> producers = getFilteredActiveProducers(clientId, producerId, filter);
        List<Long> dataProviderIds = collectDataProviderIds(producers);

        // Get allowed consumers (not directly used but might be needed for side effects)
        consumerService.getConsumersOfProviders(dataProviderIds);

        populateConsumersForProducers(producers);

        return ProducerConfigDTO.builder()
                .clientId(clientId)
                .producers(producers)
                .build();
    }

    /**
     * Filters consumers by client ID, optional consumer ID, and an optional caller filter -
     * both narrowing conditions are pushed to the database as a single {@link Specification}
     * rather than fetched then narrowed in Java.
     *
     * @param clientId the client ID
     * @param consumerId the optional consumer ID
     * @param filter an optional validated caller filter
     * @return a list of filtered consumers
     */
    private List<ConsumerDTO> getFilteredConsumers(
            String clientId, Optional<Long> consumerId, Optional<FilterNode> filter) {
        Specification<Consumer> idAndFilterSpec = idAndFilterSpecification(consumerId, filter, ResourceType.CONSUMER);
        return consumerService.findByIdpClientId(clientId, idAndFilterSpec);
    }

    /**
     * Filters active producers by client ID, optional producer ID, and an optional caller
     * filter - id/filter narrowing is pushed to the database as a single {@link Specification};
     * the {@code active} business rule stays a post-fetch Java filter, unchanged from before.
     *
     * @param clientId the client ID
     * @param producerId the optional producer ID
     * @param filter an optional validated caller filter
     * @return a list of filtered active producers
     */
    private List<ProducerDTO> getFilteredActiveProducers(
            String clientId, Optional<Long> producerId, Optional<FilterNode> filter) {
        Specification<Producer> idAndFilterSpec = idAndFilterSpecification(producerId, filter, ResourceType.PRODUCER);
        return producerService.getProducersByClientId(clientId, idAndFilterSpec).stream()
                .filter(ProducerDTO::getActive)
                .toList();
    }

    /**
     * Builds the id-equality predicate and/or the compiled caller-filter predicate, AND-ed
     * together. Returns {@code null} (no additional restriction beyond client scoping, applied
     * by the service layer) when neither is present - preserving pre-existing unfiltered
     * behaviour.
     */
    private <T> Specification<T> idAndFilterSpecification(
            Optional<Long> id, Optional<FilterNode> filter, ResourceType resourceType) {
        Specification<T> spec = id.map(value -> (Specification<T>) (root, query, cb) -> cb.equal(root.get("id"), value))
                .orElse(null);
        if (filter.isPresent()) {
            Specification<T> filterSpec = specificationPredicateCompiler.compile(resourceType, filter.get());
            spec = spec == null ? filterSpec : spec.and(filterSpec);
        }
        return spec;
    }

    /**
     * Collects data provider IDs from a list of producers.
     *
     * @param producers the list of producers
     * @return a list of data provider IDs
     */
    private List<Long> collectDataProviderIds(List<ProducerDTO> producers) {
        List<Long> dataProviderIds = new ArrayList<>();

        for (ProducerDTO producer : producers) {
            List<Long> ids =
                    producer.getProducts().stream().map(ProductDTO::getId).toList();
            dataProviderIds.addAll(ids);
        }

        return dataProviderIds;
    }

    /**
     * Resolves consumers for each product and populates them onto the product DTOs,
     * filtering out consumers whose organisations have inactive certificates.
     *
     * @param producers the list of producers whose products need consumer resolution
     */
    private void populateConsumersForProducers(List<ProducerDTO> producers) {
        // Resolve all valid consumers per product
        Map<ProductDTO, List<ConsumerDTO>> consumersByProduct = new LinkedHashMap<>();
        for (ProducerDTO producer : producers) {
            for (ProductDTO product : producer.getProducts()) {
                List<ConsumerDTO> resolved = productConsumerService.findByDataProviderId(product.getId()).stream()
                        .filter(this::isValidProvider)
                        .map(cp -> consumerService.findById(cp.getConsumerId()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();
                consumersByProduct.put(product, resolved);
            }
        }

        Set<Long> allConsumerOrgIds = consumersByProduct.values().stream()
                .flatMap(List::stream)
                .map(ConsumerDTO::getOrgId)
                .collect(Collectors.toSet());
        Set<Long> activeOrgIds = certificateValidationProvider.findActiveOrganisationIds(allConsumerOrgIds);

        // Populate each product's consumer list, skipping inactive orgs
        for (var entry : consumersByProduct.entrySet()) {
            ProductDTO product = entry.getKey();
            if (product.getConsumers() == null) {
                product.setConsumers(new ArrayList<>());
            }
            for (ConsumerDTO consumer : entry.getValue()) {
                if (activeOrgIds.contains(consumer.getOrgId())) {
                    product.getConsumers().add(consumer);
                }
            }
        }
    }

    /**
     * Checks if a provider (product consumer) is valid based on its granted date and validity period.
     *
     * @param provider the product consumer DTO
     * @return true if valid, false otherwise
     */
    private boolean isValidProvider(ProductConsumerDTO provider) {

        if (provider.getValidity() == null || provider.getValidity().equals(BigDecimal.ZERO)) return true;

        return isValidGrantedTs(provider.getGrantedTs(), provider.getValidity());
    }

    /**
     * Filters producers to only those whose organisations have active certificates.
     *
     * @param producers the list of producers to filter
     * @return producers with active organisation certificates
     */
    private List<ProducerDTO> filterProducersByActiveCertificate(List<ProducerDTO> producers) {
        Set<Long> producerOrgIds = producers.stream().map(ProducerDTO::getOrgId).collect(Collectors.toSet());

        if (producerOrgIds.isEmpty()) {
            return producers;
        }

        Set<Long> activeOrgIds = certificateValidationProvider.findActiveOrganisationIds(producerOrgIds);

        return producers.stream()
                .filter(p -> activeOrgIds.contains(p.getOrgId()))
                .toList();
    }
}
