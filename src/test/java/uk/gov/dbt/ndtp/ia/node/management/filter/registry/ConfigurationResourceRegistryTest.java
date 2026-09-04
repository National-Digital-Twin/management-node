/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException;

class ConfigurationResourceRegistryTest {

    private DynamicAttributeResolver dynamicAttributeResolver;
    private ConfigurationResourceRegistry registry;

    @BeforeEach
    void setUp() {
        dynamicAttributeResolver = mock(DynamicAttributeResolver.class);
        registry = new ConfigurationResourceRegistry(dynamicAttributeResolver);
    }

    @Test
    void producerDefinition_exposesExpectedFixedColumns() {
        assertThat(registry.fixedDefinitionFor(ResourceType.PRODUCER).attributeNames())
                .containsExactlyInAnyOrder("id", "name", "description", "active", "host", "port", "tls", "orgId");
    }

    @Test
    void consumerDefinition_exposesExpectedFixedColumns() {
        assertThat(registry.fixedDefinitionFor(ResourceType.CONSUMER).attributeNames())
                .containsExactlyInAnyOrder("id", "name", "scheduleType", "scheduleExpression", "orgId");
    }

    @Test
    void resolve_returnsFixedAttributeWithoutConsultingDynamicResolver() {
        ResourceAttribute resolved = registry.resolve(ResourceType.PRODUCER, "active");

        assertThat(resolved).isInstanceOf(ResourceAttribute.Fixed.class);
        assertThat(((ResourceAttribute.Fixed) resolved).jpaPath()).isEqualTo("active");
    }

    @Test
    void resolve_fallsThroughToDynamicResolverWhenNotFixed() {
        ResourceAttribute.Dynamic dynamic =
                new ResourceAttribute.Dynamic("policy.risk-tier", 42L, AttributeType.STRING, false);
        when(dynamicAttributeResolver.resolve(ResourceType.PRODUCER, "policy.risk-tier"))
                .thenReturn(Optional.of(dynamic));

        ResourceAttribute resolved = registry.resolve(ResourceType.PRODUCER, "policy.risk-tier");

        assertThat(resolved).isEqualTo(dynamic);
    }

    @Test
    void resolve_rejectsUnknownAttributeAsRequestOrigin() {
        when(dynamicAttributeResolver.resolve(ResourceType.PRODUCER, "nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registry.resolve(ResourceType.PRODUCER, "nope"))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(FilterCompilationException.Origin.REQUEST);
    }
}
