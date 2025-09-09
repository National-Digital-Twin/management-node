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
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Producer;

@Repository
public interface ProducerRepository extends JpaRepository<Producer, Long> {

    @Query("SELECT o FROM Producer o JOIN FETCH o.products WHERE o.id IN :ids")
    List<Producer> findByIds(List<Long> ids);

    @Query("SELECT o FROM Producer o JOIN FETCH o.products WHERE o.idpClientId IN :idpClientId")
    List<Producer> findByIdpClientId(String idpClientId);
}
