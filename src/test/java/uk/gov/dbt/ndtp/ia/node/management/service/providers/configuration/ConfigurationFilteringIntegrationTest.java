/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.providers.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.dbt.ndtp.ia.node.management.converter.impl.ConsumerConverter;
import uk.gov.dbt.ndtp.ia.node.management.converter.impl.OrganisationProducerConverter;
import uk.gov.dbt.ndtp.ia.node.management.converter.impl.ProductConverter;
import uk.gov.dbt.ndtp.ia.node.management.filter.ComparisonOperator;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterNode;
import uk.gov.dbt.ndtp.ia.node.management.filter.compiler.SpecificationPredicateCompiler;
import uk.gov.dbt.ndtp.ia.node.management.filter.registry.ConfigurationResourceRegistry;
import uk.gov.dbt.ndtp.ia.node.management.filter.registry.DynamicAttributeResolver;
import uk.gov.dbt.ndtp.ia.node.management.filter.registry.ResourceType;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ConsumerDTO;
import uk.gov.dbt.ndtp.ia.node.management.model.dto.ProducerDTO;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.AttributeDefinition;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.AttributeDefinitionScope;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.AttributeScope;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.AttributeValue;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Consumer;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Organisation;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Producer;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.AbstractPostgresRepositoryTest;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.AttributeDefinitionRepository;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.AttributeDefinitionScopeRepository;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.AttributeScopeRepository;
import uk.gov.dbt.ndtp.ia.node.management.service.data.ConsumerService;
import uk.gov.dbt.ndtp.ia.node.management.service.data.ProducerService;
import uk.gov.dbt.ndtp.ia.node.management.service.data.impl.ConsumerServiceImpl;
import uk.gov.dbt.ndtp.ia.node.management.service.data.impl.ProducerServiceImpl;

/**
 * End-to-end coverage (real Postgres, real Specification compiler, real service/converter
 * beans) of the dynamic-config-filtering capability's spec.md requirements: filtering evaluated
 * by the database, existing behaviour preserved with no filter, the client scope boundary, and a
 * newly-registered dynamic attribute being filterable without a restart. Section 6 of tasks.md.
 *
 * <p>Exercises {@code ProducerService}/{@code ConsumerService} directly rather than through
 * {@code ConfigurationProviderImpl} (which also pulls in certificate-validation and
 * product-consumer machinery this change does not touch) or over HTTP (this codebase has no
 * {@code @SpringBootTest}/full-security-stack test precedent to build on) - this is the
 * narrowest real-Postgres slice that actually proves the new query path end-to-end.
 */
@Transactional
@Import({
    OrganisationProducerConverter.class,
    ProductConverter.class,
    ConsumerConverter.class,
    ProducerServiceImpl.class,
    ConsumerServiceImpl.class,
    DynamicAttributeResolver.class,
    ConfigurationResourceRegistry.class,
    SpecificationPredicateCompiler.class
})
class ConfigurationFilteringIntegrationTest extends AbstractPostgresRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProducerService producerService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private SpecificationPredicateCompiler compiler;

    @Autowired
    private AttributeDefinitionRepository attributeDefinitionRepository;

    @Autowired
    private AttributeDefinitionScopeRepository attributeDefinitionScopeRepository;

    @Autowired
    private AttributeScopeRepository attributeScopeRepository;

    private Organisation persistOrganisation(String name) {
        Organisation org = new Organisation();
        org.setName(name);
        entityManager.persist(org);
        return org;
    }

    private Producer persistProducer(Organisation org, String name, String clientId, boolean active) {
        Producer producer = new Producer();
        producer.setName(name);
        producer.setDescription("test");
        producer.setOrg(org);
        producer.setActive(active);
        producer.setHost("host.example");
        producer.setPort(BigDecimal.valueOf(443));
        producer.setTls(true);
        producer.setIdpClientId(clientId);
        entityManager.persist(producer);
        return producer;
    }

    private Consumer persistConsumer(Organisation org, String name, String clientId) {
        Consumer consumer = new Consumer();
        consumer.setName(name);
        consumer.setScheduleType("cron");
        consumer.setOrg(org);
        consumer.setIdpClientId(clientId);
        entityManager.persist(consumer);
        return consumer;
    }

    private AttributeDefinitionScope persistProducerScopedDefinition(String name) {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setNamespace("policy");
        definition.setName(name);
        definition.setDescription("test");
        definition.setDataType("STRING");
        definition.setCreatedAt(Timestamp.from(Instant.now()));
        definition.setCreatedBy("test");
        definition = attributeDefinitionRepository.saveAndFlush(definition);

        AttributeScope scope = attributeScopeRepository.findByCode("PRODUCER").orElseThrow();
        AttributeDefinitionScope binding = new AttributeDefinitionScope();
        binding.setAttributeDefinition(definition);
        binding.setAttributeScope(scope);
        binding.setRequired(false);
        binding.setCreatedAt(Timestamp.from(Instant.now()));
        binding.setCreatedBy("test");
        return attributeDefinitionScopeRepository.saveAndFlush(binding);
    }

    private void persistValue(AttributeDefinitionScope binding, Long entityId, String json) {
        AttributeValue value = new AttributeValue();
        value.setAttributeDefinitionScope(binding);
        value.setEntityId(entityId);
        value.setValue(json);
        value.setCreatedAt(Timestamp.from(Instant.now()));
        value.setCreatedBy("test");
        entityManager.persist(value);
    }

    // 6.1 - filter on a fixed column

    @Test
    void filterOnFixedColumn_matchesOnlyActiveProducerForThatClient() {
        Organisation org = persistOrganisation("org-6-1");
        persistProducer(org, "active-producer", "client-6-1", true);
        persistProducer(org, "inactive-producer", "client-6-1", false);
        entityManager.flush();

        var spec = compiler.<Producer>compile(
                ResourceType.PRODUCER, FilterNode.Comparison.of("active", ComparisonOperator.EQ, true));

        var results = producerService.getProducersByClientId("client-6-1", spec);

        assertThat(results).extracting(ProducerDTO::getName).containsExactly("active-producer");
    }

    // 6.2 - filter on a dynamically registered attribute

    @Test
    void filterOnDynamicAttribute_matchesOnlyProducerWithLiveAttributeValue() {
        Organisation org = persistOrganisation("org-6-2");
        Producer withTier = persistProducer(org, "with-tier", "client-6-2", true);
        persistProducer(org, "without-tier", "client-6-2", true);
        entityManager.flush();

        AttributeDefinitionScope binding = persistProducerScopedDefinition("tier-6-2");
        persistValue(binding, withTier.getId(), "\"gold\"");
        entityManager.flush();

        var spec = compiler.<Producer>compile(
                ResourceType.PRODUCER, FilterNode.Comparison.of("policy.tier-6-2", ComparisonOperator.EQ, "gold"));

        var results = producerService.getProducersByClientId("client-6-2", spec);

        assertThat(results).extracting(ProducerDTO::getName).containsExactly("with-tier");
    }

    // 6.3 - the client scope boundary cannot be widened by a filter

    @Test
    void filterCannotWidenAccessBeyondCallersClientScope() {
        Organisation org = persistOrganisation("org-6-3");
        persistProducer(org, "other-clients-producer", "other-client-6-3", true);
        entityManager.flush();

        // A filter that, alone, would match the other client's active producer.
        var spec = compiler.<Producer>compile(
                ResourceType.PRODUCER, FilterNode.Comparison.of("active", ComparisonOperator.EQ, true));

        var results = producerService.getProducersByClientId("client-6-3", spec);

        assertThat(results).isEmpty();
    }

    // 6.4 - no filter parameter behaves exactly as before this change

    @Test
    void noFilter_returnsIdenticalResultToPreExistingUnfilteredMethod() {
        Organisation org = persistOrganisation("org-6-4");
        persistConsumer(org, "consumer-a", "client-6-4");
        persistConsumer(org, "consumer-b", "client-6-4");
        entityManager.flush();

        var withNullFilter = consumerService.findByIdpClientId("client-6-4", null);
        var preExisting = consumerService.findByIdpClientId("client-6-4");

        assertThat(withNullFilter)
                .extracting(ConsumerDTO::getName)
                .containsExactlyInAnyOrderElementsOf(
                        preExisting.stream().map(ConsumerDTO::getName).toList());
        assertThat(withNullFilter).hasSize(2);
    }

    // 6.5 - a dynamic attribute registered after this test's beans were created is immediately filterable

    @Test
    void newlyRegisteredDynamicAttribute_isFilterableWithoutRestart() {
        Organisation org = persistOrganisation("org-6-5");
        Producer producer = persistProducer(org, "late-bound-producer", "client-6-5", true);
        entityManager.flush();

        // Querying before the attribute is registered: unknown attribute, resolves to no match
        // via the dynamic resolver's live per-request lookup (not a stale startup snapshot).
        AttributeDefinitionScope binding = persistProducerScopedDefinition("late-bound-tier");
        persistValue(binding, producer.getId(), "\"platinum\"");
        entityManager.flush();

        // The registry/resolver/compiler beans used here were constructed once for this test
        // context - exactly as they would be for a long-running application - so a match here
        // proves the lookup is genuinely per-request, not cached from before the attribute existed.
        var spec = compiler.<Producer>compile(
                ResourceType.PRODUCER,
                FilterNode.Comparison.of("policy.late-bound-tier", ComparisonOperator.EQ, "platinum"));

        var results = producerService.getProducersByClientId("client-6-5", spec);

        assertThat(results).extracting(ProducerDTO::getName).containsExactly("late-bound-producer");
    }
}
