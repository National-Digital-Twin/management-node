package uk.gov.dbt.ndtp.ia.node.management.persistency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.ProductConsumer;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.ProductConsumerId;

import java.util.List;

@Repository
public interface ConsumerProviderRepository extends JpaRepository<ProductConsumer, ProductConsumerId> {

    @Query("Select dp from ProductConsumer  dp where dp.id.consumerId=:consumerId")
    List<ProductConsumer> findByConsumerId(@Param("consumerId") Long consumerId);

    @Query("Select dp from ProductConsumer  dp where dp.id.productId=:productId")
    List<ProductConsumer> findByProductId(Long productId);
}