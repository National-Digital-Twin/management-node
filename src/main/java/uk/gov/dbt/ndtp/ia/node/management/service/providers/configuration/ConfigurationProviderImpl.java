/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
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
import uk.gov.dbt.ndtp.ia.node.management.service.data.ProductService;

@Service
public class ConfigurationProviderImpl implements ConfigurationProvider {

    private final ConsumerService consumerService;

    private final ProductConsumerService consumerAllowedDataProvidersService;


    private final ProducerService producerService;

    public ConfigurationProviderImpl(
            ConsumerService consumerService,
            ProductConsumerService consumerAllowedDataProviders,
            ProductService dataProviderService,
            ProducerService producerService) {

        this.consumerService = consumerService;
        this.consumerAllowedDataProvidersService = consumerAllowedDataProviders;
        this.producerService = producerService;
    }

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
        
        List<Long> validProductIds = new ArrayList<>();

        consumers.forEach(consumer -> validProductIds.addAll(
                consumerAllowedDataProvidersService.findByConsumerId(consumer.getId()).stream()
                        .filter(this::isValidProvider)
                        .map(ProductConsumerDTO::getProductId)
                        .toList()
        ));



        List<ProducerDTO> producers = producerService.getProducersByConsumerIds(consumerIds).stream()
                .filter(ProducerDTO::getActive)
                .toList();

        return ConsumerConfigDTO.builder()
                .clientId(clientId)
                .producers(producers)
                .build();
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
     * @param clientId   the client ID to filter by
     * @param consumerId optional consumer ID for additional filtering
     * @return filtered list of consumers
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
     * @param clientId   the client ID to filter by
     * @param producerId optional producer ID for additional filtering
     * @return filtered list of active producers
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
     * Collects all data provider IDs from the given producers.
     *
     * @param producers list of producers
     * @return list of data provider IDs
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
    private void processConsumersForProvider(ProductDTO provider) {

        // Get consumer providers for this data provider
        List<ProductConsumerDTO> consumerProviders =
                consumerAllowedDataProvidersService.findByDataProviderId(provider.getId());

        // Filter valid providers and add their consumers
        addValidConsumersToProvider(consumerProviders, provider);
    }

    /**
     * Adds valid consumers to the given provider.
     *
     * @param consumerProviders list of consumer-provider relationships
     * @param provider          the provider to add consumers to
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

    private boolean isValidProvider(ProductConsumerDTO provider) {

        if (provider.getValidity() == null || provider.getValidity().equals(BigDecimal.ZERO)) return true;

        return isValidGrantedTs(provider.getGrantedTs(), provider.getValidity());
    }
}
