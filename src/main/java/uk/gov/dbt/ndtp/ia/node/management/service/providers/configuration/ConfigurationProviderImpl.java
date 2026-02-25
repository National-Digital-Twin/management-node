/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.providers.configuration;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.*;
import uk.gov.dbt.ndtp.ia.node.management.service.data.ConsumerService;
import uk.gov.dbt.ndtp.ia.node.management.service.data.ProducerService;
import uk.gov.dbt.ndtp.ia.node.management.service.data.ProductConsumerService;

/**
 * Implementation of {@link ConfigurationProvider} that retrieves configuration from database services.
 */
@Service
public class ConfigurationProviderImpl implements ConfigurationProvider {

    private final ConsumerService consumerService;

    private final ProductConsumerService productConsumerService;

    private final ProducerService producerService;

    /**
     * Constructs a new ConfigurationProviderImpl with required services.
     *
     * @param consumerService the consumer service
     * @param consumerAllowedDataProviders the product consumer service
     * @param producerService the producer service
     */
    public ConfigurationProviderImpl(
            ConsumerService consumerService,
            ProductConsumerService consumerAllowedDataProviders,
            ProducerService producerService) {

        this.consumerService = consumerService;
        this.productConsumerService = consumerAllowedDataProviders;
        this.producerService = producerService;
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
                        .plus(java.time.Duration.ofDays(validity.longValue()))
                        .isAfter(Instant.now());
    }

    @Override
    public ConsumerConfigDTO getConsumerConfigByClientId(String clientId, Optional<Long> consumerId) {
        List<ConsumerDTO> consumers = getFilteredConsumers(clientId, consumerId);
        List<Long> consumerIds = consumers.stream().map(ConsumerDTO::getId).toList();

        List<ProductConsumerDTO> validProductConsumers = getValidProductConsumers(consumers);
        List<Long> validProductIds = validProductConsumers.stream()
                .map(ProductConsumerDTO::getProductId)
                .toList();

        List<ProducerDTO> producers = producerService.getProducersByConsumerIds(consumerIds).stream()
                .filter(ProducerDTO::getActive)
                .toList();

        // Filter products of each producer to only those in validProductIds
        if (!validProductIds.isEmpty()) {
            var validIdsSet = new java.util.HashSet<>(validProductIds);
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
        List<ProducerDTO> producers = getFilteredActiveProducers(clientId, producerId);
        List<Long> dataProviderIds = collectDataProviderIds(producers);

        // Get allowed consumers (not directly used but might be needed for side effects)
        consumerService.getConsumersOfProviders(dataProviderIds);

        // Process consumers for each provider
        processConsumersForProducers(producers);

        return ProducerConfigDTO.builder()
                .clientId(clientId)
                .producers(producers)
                .build();
    }

    /**
     * Filters consumers by client ID and optional consumer ID.
     *
     * @param clientId the client ID
     * @param consumerId the optional consumer ID
     * @return a list of filtered consumers
     */
    private List<ConsumerDTO> getFilteredConsumers(String clientId, Optional<Long> consumerId) {
        List<ConsumerDTO> consumers = consumerService.findByIdpClientId(clientId);

        if (consumerId.isPresent()) {
            consumers = consumers.stream()
                    .filter(consumer -> consumer.getId().equals(consumerId.get()))
                    .toList();
        }

        return consumers;
    }

    /**
     * Filters active producers by client ID and optional producer ID.
     *
     * @param clientId the client ID
     * @param producerId the optional producer ID
     * @return a list of filtered active producers
     */
    private List<ProducerDTO> getFilteredActiveProducers(String clientId, Optional<Long> producerId) {
        List<ProducerDTO> producers = producerService.getProducersByClientId(clientId).stream()
                .filter(ProducerDTO::getActive)
                .toList();

        if (producerId.isPresent()) {
            producers = producers.stream()
                    .filter(producer -> producerId.get().equals(producer.getId()))
                    .toList();
        }

        return producers;
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
     * Processes consumers for each provider in the given producers.
     *
     * @param producers list of producers to process
     */
    /**
     * Processes consumers for a list of producers.
     *
     * @param producers the list of producers
     */
    private void processConsumersForProducers(List<ProducerDTO> producers) {
        for (ProducerDTO producer : producers) {
            for (ProductDTO provider : producer.getProducts()) {
                processConsumersForProvider(provider);
            }
        }
    }

    /**
     * Processes consumers for a specific provider.
     *
     * @param provider the provider to process consumers for
     */
    /**
     * Processes consumers for a specific provider.
     *
     * @param provider the product provider DTO
     */
    private void processConsumersForProvider(ProductDTO provider) {

        // Get consumer providers for this data provider
        List<ProductConsumerDTO> consumerProviders = productConsumerService.findByDataProviderId(provider.getId());

        // Filter valid providers and add their consumers
        addValidConsumersToProvider(consumerProviders, provider);
    }

    /**
     * Adds valid consumers to the given provider.
     *
     * @param consumerProviders list of consumer-provider relationships
     * @param provider          the provider to add consumers to
     */
    /**
     * Adds valid consumers to a provider.
     *
     * @param consumerProviders the list of product consumer DTOs
     * @param provider the product provider DTO
     */
    private void addValidConsumersToProvider(List<ProductConsumerDTO> consumerProviders, ProductDTO provider) {
        if (provider.getConsumers() == null) {
            provider.setConsumers(new ArrayList<>());
        }
        consumerProviders.stream().filter(this::isValidProvider).forEach(consumerProvider -> {
            Optional<ConsumerDTO> consumer = consumerService.findById(consumerProvider.getConsumerId());
            consumer.ifPresent(provider.getConsumers()::add);
        });
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
}
