package uk.gov.dbt.ndtp.ia.node.management.persistency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Producer;

import java.util.List;

@Repository
public interface ProducerRepository extends JpaRepository<Producer, Long> {

    @Query("SELECT o FROM Producer o JOIN FETCH o.products WHERE o.id IN :ids")
    List<Producer> findByIds(List<Long> ids);

    @Query("SELECT o FROM Producer o JOIN FETCH o.products WHERE o.idpClientId IN :idpClientId")
    List<Producer> findByIdpClientId (String idpClientId);
}