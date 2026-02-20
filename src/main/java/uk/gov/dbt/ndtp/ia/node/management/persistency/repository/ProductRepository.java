/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.persistency.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Product;

/**
 * Repository interface for managing {@link Product} entities.
 *
 * This interface extends {@link JpaRepository} to provide CRUD operations and additional
 * query methods to interact with the {@link Product} database entity. It focuses on enabling
 * functionality specific to retrieving products based on identifiers or associated producers.
 *
 * The primary focus is on the {@link Product} entity with the identifier type {@link Long}.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Retrieves a list of {@link Product} entities based on the provided list of product IDs.
     *
     * @param ids a list of product IDs for which the {@link Product} entities are to be retrieved
     * @return a list of {@link Product} entities matching the provided IDs
     */
    @Query("SELECT o FROM Product o " + "JOIN FETCH o.productType t " + "WHERE o.id IN :ids")
    List<Product> findByIds(List<Long> ids);

    /**
     * Retrieves a list of {@link Product} entities associated with the specified producer IDs.
     *
     * @param producers a list of producer IDs whose associated {@link Product} entities need to be retrieved
     * @return a list of {@link Product} entities linked to the specified producer IDs
     */
    @Query("SELECT o FROM Product o " + "JOIN FETCH o.productType t " + " WHERE o.producer.id IN :producers")
    List<Product> findByProducerIds(List<Long> producers);
}
