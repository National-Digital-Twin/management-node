/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.persistency.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Consumer;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Organisation;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Producer;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Product;

/**
 * Verifies task 4.1 (repositories are wired for {@link Specification}-based queries) and task 4.3
 * (the fetch gap left by moving off {@code JOIN FETCH} is closed with an {@code @EntityGraph} so
 * the filtered path does not N+1-load {@code products}/{@code productConsumers}).
 */
@Transactional
class ProducerConsumerSpecificationRepositoryTest extends AbstractPostgresRepositoryTest {

    @DynamicPropertySource
    static void statisticsProperty(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.properties.hibernate.generate_statistics", () -> "true");
    }

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private ProducerRepository producerRepository;

    @Autowired
    private ConsumerRepository consumerRepository;

    private Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    @Test
    void producerFindAllBySpecification_matchesExpectedRowsAndFetchJoinsProducts() {
        Organisation org = new Organisation();
        org.setName("spec-org");
        entityManager.persist(org);

        Producer producer = new Producer();
        producer.setName("spec-producer");
        producer.setDescription("test");
        producer.setOrg(org);
        producer.setActive(true);
        producer.setHost("host.example");
        producer.setPort(java.math.BigDecimal.valueOf(443));
        producer.setTls(true);
        producer.setIdpClientId("spec-producer-client");
        entityManager.persist(producer);

        Product product = new Product();
        product.setName("spec-product");
        product.setTopic("spec-topic");
        product.setProducer(producer);
        entityManager.persist(product);

        entityManager.flush();
        entityManager.clear();
        statistics().clear();

        Specification<Producer> byId = (root, query, cb) -> cb.equal(root.get("id"), producer.getId());
        java.util.List<Producer> results = producerRepository.findAll(byId);

        assertThat(results).hasSize(1);
        // Accessing products must not trigger an additional lazy-load query - proves the
        // @EntityGraph fetch-join, not N+1, populated the association.
        assertThat(results.getFirst().getProducts()).hasSize(1);
        assertThat(statistics().getQueryExecutionCount()).isEqualTo(1);
    }

    @Test
    void consumerFindAllBySpecification_returnsMatchingRowOnly() {
        Organisation org = new Organisation();
        org.setName("spec-consumer-org");
        entityManager.persist(org);

        Consumer matching = new Consumer();
        matching.setName("matching-consumer");
        matching.setScheduleType("cron");
        matching.setOrg(org);
        matching.setIdpClientId("spec-consumer-client");
        entityManager.persist(matching);

        Consumer other = new Consumer();
        other.setName("other-consumer");
        other.setScheduleType("cron");
        other.setOrg(org);
        other.setIdpClientId("other-consumer-client");
        entityManager.persist(other);

        entityManager.flush();
        entityManager.clear();

        Specification<Consumer> byName = (root, query, cb) -> cb.equal(root.get("name"), "matching-consumer");
        java.util.List<Consumer> results = consumerRepository.findAll(byName);

        assertThat(results).extracting(Consumer::getName).containsExactly("matching-consumer");
    }
}
