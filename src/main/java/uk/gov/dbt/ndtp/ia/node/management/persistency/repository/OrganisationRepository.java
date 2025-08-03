package uk.gov.dbt.ndtp.ia.node.management.persistency.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Organisation;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
}