/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter.registry;

/**
 * The configuration endpoints' filterable resource types, each tied to the {@code attribute_scope.code}
 * row that scopes its dynamic attributes.
 */
public enum ResourceType {
    PRODUCER("PRODUCER"),
    CONSUMER("CONSUMER");

    private final String attributeScopeCode;

    ResourceType(String attributeScopeCode) {
        this.attributeScopeCode = attributeScopeCode;
    }

    /** The {@code attribute_scope.code} that scopes this resource type's dynamic attributes. */
    public String attributeScopeCode() {
        return attributeScopeCode;
    }
}
