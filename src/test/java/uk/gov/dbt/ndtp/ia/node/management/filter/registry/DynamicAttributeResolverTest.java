/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException.Origin;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.AttributeDefinition;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.AttributeDefinitionScope;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.AttributeScope;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.AbstractPostgresRepositoryTest;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.AttributeDefinitionRepository;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.AttributeDefinitionScopeRepository;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.AttributeScopeRepository;

class DynamicAttributeResolverTest extends AbstractPostgresRepositoryTest {

    @Autowired
    private AttributeDefinitionRepository attributeDefinitionRepository;

    @Autowired
    private AttributeDefinitionScopeRepository attributeDefinitionScopeRepository;

    @Autowired
    private AttributeScopeRepository attributeScopeRepository;

    private DynamicAttributeResolver resolver;

    private DynamicAttributeResolver resolver() {
        if (resolver == null) {
            resolver = new DynamicAttributeResolver(attributeDefinitionRepository, attributeDefinitionScopeRepository);
        }
        return resolver;
    }

    private AttributeDefinition persistDefinition(String namespace, String name, String dataType, boolean multi) {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setNamespace(namespace);
        definition.setName(name);
        definition.setDescription("test");
        definition.setDataType(dataType);
        definition.setMultiValued(multi);
        definition.setCreatedAt(Timestamp.from(Instant.now()));
        definition.setCreatedBy("test");
        return attributeDefinitionRepository.saveAndFlush(definition);
    }

    private void bindToScope(AttributeDefinition definition, String scopeCode) {
        AttributeScope scope = attributeScopeRepository.findByCode(scopeCode).orElseThrow();
        AttributeDefinitionScope binding = new AttributeDefinitionScope();
        binding.setAttributeDefinition(definition);
        binding.setAttributeScope(scope);
        binding.setRequired(false);
        binding.setCreatedAt(Timestamp.from(Instant.now()));
        binding.setCreatedBy("test");
        attributeDefinitionScopeRepository.saveAndFlush(binding);
    }

    @Test
    void resolve_returnsDynamicAttributeForRegisteredScope() {
        AttributeDefinition definition = persistDefinition("policy", "risk-tier", "STRING", false);
        bindToScope(definition, "PRODUCER");

        Optional<ResourceAttribute.Dynamic> resolved = resolver().resolve(ResourceType.PRODUCER, "policy.risk-tier");

        assertThat(resolved).isPresent();
        assertThat(resolved.get().type()).isEqualTo(AttributeType.STRING);
        assertThat(resolved.get().multiValued()).isFalse();
    }

    @Test
    void resolve_isEmptyWhenDefinitionExistsButNotBoundToRequestedScope() {
        AttributeDefinition definition = persistDefinition("policy", "consumer-only", "STRING", false);
        bindToScope(definition, "CONSUMER");

        assertThat(resolver().resolve(ResourceType.PRODUCER, "policy.consumer-only"))
                .isEmpty();
    }

    @Test
    void resolve_isEmptyForUnregisteredAttributeName() {
        assertThat(resolver().resolve(ResourceType.PRODUCER, "policy.does-not-exist"))
                .isEmpty();
    }

    @Test
    void resolve_isEmptyForMalformedLogicalName() {
        assertThat(resolver().resolve(ResourceType.PRODUCER, "no-dot-here")).isEmpty();
    }

    @Test
    void resolve_throwsPolicyOriginForUnrecognisedDataType() {
        AttributeDefinition definition = persistDefinition("policy", "bad-type", "XML", false);
        bindToScope(definition, "PRODUCER");
        DynamicAttributeResolver resolver = resolver();

        assertThatThrownBy(() -> resolver.resolve(ResourceType.PRODUCER, "policy.bad-type"))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(Origin.POLICY);
    }

    @Test
    void resolve_isEmptyWhenDefinitionIsSoftDeleted() {
        AttributeDefinition definition = persistDefinition("policy", "deleted-attr", "STRING", false);
        bindToScope(definition, "PRODUCER");
        definition.setIsDeleted(true);
        attributeDefinitionRepository.saveAndFlush(definition);

        assertThat(resolver().resolve(ResourceType.PRODUCER, "policy.deleted-attr"))
                .isEmpty();
    }
}
