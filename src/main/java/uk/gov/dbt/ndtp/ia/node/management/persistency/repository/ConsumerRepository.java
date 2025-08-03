package uk.gov.dbt.ndtp.ia.node.management.persistency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Consumer;

import java.util.List;

@Repository
public interface ConsumerRepository extends JpaRepository<Consumer, Long> {

    List<Consumer> findByIdpClientId(String clientId);

    @Query("SELECT c FROM Consumer c  JOIN c.productConsumers cp WHERE cp.id.productId  IN :providers")
    List<Consumer> findConsumersByProviderIds(List<Long> providers);

}