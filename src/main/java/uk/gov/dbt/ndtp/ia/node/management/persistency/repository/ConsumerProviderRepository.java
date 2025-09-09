/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2025. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.persistency.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.ProductConsumer;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.ProductConsumerId;

@Repository
public interface ConsumerProviderRepository extends JpaRepository<ProductConsumer, ProductConsumerId> {

    @Query("Select dp from ProductConsumer  dp where dp.id.consumerId=:consumerId")
    List<ProductConsumer> findByConsumerId(@Param("consumerId") Long consumerId);

    @Query("Select dp from ProductConsumer  dp where dp.id.productId=:productId")
    List<ProductConsumer> findByProductId(Long productId);
}
