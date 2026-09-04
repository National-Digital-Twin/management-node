/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter.registry;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The closed set of fixed columns filterable on one resource type. Read from Java, not the
 * database - these columns are only ever added by a Flyway-owned migration, so unlike dynamic
 * attributes they do not need to be resolvable without a deploy.
 */
public record ResourceDefinition(ResourceType resourceType, Map<String, ResourceAttribute.Fixed> attributes) {

    public ResourceDefinition {
        attributes = Map.copyOf(attributes);
    }

    public Optional<ResourceAttribute.Fixed> find(String logicalName) {
        return Optional.ofNullable(attributes.get(logicalName));
    }

    public List<String> attributeNames() {
        return attributes.keySet().stream().sorted().toList();
    }
}
