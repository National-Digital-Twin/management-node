/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter.registry;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException.Origin;

/**
 * Resolves a caller's logical attribute name, for {@code producer}/{@code consumer} filtering,
 * against a fixed column first and a dynamically-registered attribute second - one entry point
 * regardless of which kind the name turns out to be.
 */
@Component
public class ConfigurationResourceRegistry {

    private final Map<ResourceType, ResourceDefinition> fixedDefinitions;
    private final DynamicAttributeResolver dynamicAttributeResolver;

    public ConfigurationResourceRegistry(DynamicAttributeResolver dynamicAttributeResolver) {
        this.dynamicAttributeResolver = dynamicAttributeResolver;
        this.fixedDefinitions = Map.of(
                ResourceType.PRODUCER, producerDefinition(),
                ResourceType.CONSUMER, consumerDefinition());
    }

    public ResourceDefinition fixedDefinitionFor(ResourceType resourceType) {
        return fixedDefinitions.get(resourceType);
    }

    /**
     * @throws FilterCompilationException with {@code Origin.REQUEST} if {@code logicalName}
     *     resolves to neither a fixed column nor a live dynamic attribute for {@code
     *     resourceType}
     */
    public ResourceAttribute resolve(ResourceType resourceType, String logicalName) {
        Optional<ResourceAttribute.Fixed> fixed =
                fixedDefinitionFor(resourceType).find(logicalName);
        if (fixed.isPresent()) {
            return fixed.get();
        }
        return dynamicAttributeResolver
                .resolve(resourceType, logicalName)
                .map(ResourceAttribute.class::cast)
                .orElseThrow(() -> new FilterCompilationException(
                        Origin.REQUEST,
                        "Unknown attribute '" + logicalName + "' for resource type '" + resourceType + "'"));
    }

    private static ResourceDefinition producerDefinition() {
        return new ResourceDefinition(
                ResourceType.PRODUCER,
                Map.ofEntries(
                        fixed("id", "id", AttributeType.LONG),
                        fixed("name", "name", AttributeType.STRING),
                        fixed("description", "description", AttributeType.STRING),
                        fixed("active", "active", AttributeType.BOOLEAN),
                        fixed("host", "host", AttributeType.STRING),
                        fixed("port", "port", AttributeType.DECIMAL),
                        fixed("tls", "tls", AttributeType.BOOLEAN),
                        fixed("orgId", "org.id", AttributeType.LONG)));
    }

    private static ResourceDefinition consumerDefinition() {
        return new ResourceDefinition(
                ResourceType.CONSUMER,
                Map.ofEntries(
                        fixed("id", "id", AttributeType.LONG),
                        fixed("name", "name", AttributeType.STRING),
                        fixed("scheduleType", "scheduleType", AttributeType.STRING),
                        fixed("scheduleExpression", "scheduleExpression", AttributeType.STRING),
                        fixed("orgId", "org.id", AttributeType.LONG)));
    }

    /** A fixed-column map entry, keyed by the same logical name the {@link ResourceAttribute.Fixed} carries. */
    private static Map.Entry<String, ResourceAttribute.Fixed> fixed(
            String logicalName, String jpaPath, AttributeType type) {
        return Map.entry(logicalName, new ResourceAttribute.Fixed(logicalName, jpaPath, type));
    }
}
