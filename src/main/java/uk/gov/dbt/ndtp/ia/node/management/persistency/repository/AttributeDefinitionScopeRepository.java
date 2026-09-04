/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.persistency.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.AttributeDefinitionScope;

/**
 * Repository interface for managing {@link AttributeDefinitionScope} entities.
 * Provides persistence operations and query methods for interacting with the
 * underlying database.
 *
 * Extends {@link JpaRepository} to inherit standard CRUD operations and adds
 * query methods specific to {@link AttributeDefinitionScope}.
 */
@Repository
public interface AttributeDefinitionScopeRepository extends JpaRepository<AttributeDefinitionScope, Long> {

    List<AttributeDefinitionScope> findByAttributeDefinitionId(Long attributeDefinitionId);

    /**
     * Resolves the single live binding of an attribute definition to a named scope, used to
     * correlate a dynamic filter attribute against {@code attribute_value} by a single foreign
     * key rather than joining {@code attribute_scope} at query time.
     *
     * @param attributeDefinitionId the {@code attribute_definition.id} resolved from the caller's
     *     logical attribute name
     * @param scopeCode the {@code attribute_scope.code} of the resource type being filtered
     *     (e.g. {@code "PRODUCER"}), never a caller-supplied string
     */
    Optional<AttributeDefinitionScope> findByAttributeDefinition_IdAndAttributeScope_CodeAndIsDeletedFalse(
            Long attributeDefinitionId, String scopeCode);
}
