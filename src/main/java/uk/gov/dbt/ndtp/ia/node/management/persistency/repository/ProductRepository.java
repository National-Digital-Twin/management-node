package uk.gov.dbt.ndtp.ia.node.management.persistency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT o FROM Product o WHERE o.id IN :ids")
    List<Product> findByIds(List<Long> ids);

    @Query("SELECT o FROM Product o WHERE o.producer.id IN :producers")
    List<Product> findByProducerIds(List<Long> producers);

}