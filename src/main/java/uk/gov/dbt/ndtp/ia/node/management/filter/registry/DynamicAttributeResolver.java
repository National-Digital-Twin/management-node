/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter.registry;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.AttributeDefinition;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.AttributeDefinitionScope;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.AttributeDefinitionRepository;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.AttributeDefinitionScopeRepository;

/**
 * Resolves a caller's logical attribute name to a dynamically-registered attribute, straight
 * from {@code attribute_definition}/{@code attribute_definition_scope} at filter-compile time
 * rather than a cached snapshot - so a newly-registered attribute is filterable without a
 * restart. See design.md's "dynamic attributes are resolved per lookup" decision.
 */
@Component
public class DynamicAttributeResolver {

    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final AttributeDefinitionScopeRepository attributeDefinitionScopeRepository;

    public DynamicAttributeResolver(
            AttributeDefinitionRepository attributeDefinitionRepository,
            AttributeDefinitionScopeRepository attributeDefinitionScopeRepository) {
        this.attributeDefinitionRepository = attributeDefinitionRepository;
        this.attributeDefinitionScopeRepository = attributeDefinitionScopeRepository;
    }

    /**
     * Resolves {@code logicalName} (wire shape {@code "namespace.name"}) against the live
     * attribute definitions registered for {@code resourceType}'s scope.
     *
     * @return empty when the name is not a live, registered dynamic attribute for this resource
     *     type - the registry reports this uniformly as "unknown attribute", the same as an
     *     unknown fixed column
     * @throws uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException with {@code
     *     Origin.POLICY} if the definition's declared {@code data_type} is not one this system
     *     understands - a configuration defect, since the caller never supplies this value
     */
    public Optional<ResourceAttribute.Dynamic> resolve(ResourceType resourceType, String logicalName) {
        int separator = logicalName == null ? -1 : logicalName.indexOf('.');
        if (separator <= 0 || separator == logicalName.length() - 1) {
            return Optional.empty();
        }
        String namespace = logicalName.substring(0, separator);
        String name = logicalName.substring(separator + 1);

        Optional<AttributeDefinition> definition =
                attributeDefinitionRepository.findByNamespaceAndName(namespace, name);
        if (definition.isEmpty() || Boolean.TRUE.equals(definition.get().getIsDeleted())) {
            return Optional.empty();
        }

        Optional<AttributeDefinitionScope> scope =
                attributeDefinitionScopeRepository.findByAttributeDefinition_IdAndAttributeScope_CodeAndIsDeletedFalse(
                        definition.get().getId(), resourceType.attributeScopeCode());
        if (scope.isEmpty()) {
            return Optional.empty();
        }

        AttributeType type = AttributeType.fromDataType(definition.get().getDataType());
        return Optional.of(new ResourceAttribute.Dynamic(
                logicalName,
                scope.get().getId(),
                type,
                Boolean.TRUE.equals(definition.get().getMultiValued())));
    }
}
