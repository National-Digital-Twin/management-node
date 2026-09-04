/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter.registry;

/**
 * A filterable attribute of a resource type, resolved from a caller's logical attribute name.
 * The caller addresses both kinds through the same name; only the compiler needs to know which
 * one it resolved to.
 */
public sealed interface ResourceAttribute {

    String logicalName();

    AttributeType type();

    /** A fixed entity column, resolved to its JPA property path (e.g. {@code "org.id"}). */
    record Fixed(String logicalName, String jpaPath, AttributeType type) implements ResourceAttribute {}

    /**
     * An admin-defined attribute resolved from {@code attribute_definition}/{@code attribute_definition_scope}.
     *
     * @param attributeDefinitionScopeId the resolved {@code attribute_definition_scope.id} - the
     *     only value the compiler needs to correlate against {@code attribute_value}; never a
     *     caller-supplied string
     * @param multiValued whether the definition is registered {@code multi_valued = true}
     */
    record Dynamic(String logicalName, Long attributeDefinitionScopeId, AttributeType type, boolean multiValued)
            implements ResourceAttribute {}
}
