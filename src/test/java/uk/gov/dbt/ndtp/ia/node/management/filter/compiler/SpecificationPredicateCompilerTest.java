/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter.compiler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.dbt.ndtp.ia.node.management.filter.ComparisonOperator;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException.Origin;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterNode;
import uk.gov.dbt.ndtp.ia.node.management.filter.registry.ConfigurationResourceRegistry;
import uk.gov.dbt.ndtp.ia.node.management.filter.registry.DynamicAttributeResolver;
import uk.gov.dbt.ndtp.ia.node.management.filter.registry.ResourceType;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.AttributeDefinition;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.AttributeDefinitionScope;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.AttributeScope;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.AttributeValue;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Organisation;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.Producer;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.AbstractPostgresRepositoryTest;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.AttributeDefinitionRepository;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.AttributeDefinitionScopeRepository;
import uk.gov.dbt.ndtp.ia.node.management.persistency.repository.AttributeScopeRepository;

@Transactional
class SpecificationPredicateCompilerTest extends AbstractPostgresRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AttributeDefinitionRepository attributeDefinitionRepository;

    @Autowired
    private AttributeDefinitionScopeRepository attributeDefinitionScopeRepository;

    @Autowired
    private AttributeScopeRepository attributeScopeRepository;

    private SpecificationPredicateCompiler compiler() {
        DynamicAttributeResolver resolver =
                new DynamicAttributeResolver(attributeDefinitionRepository, attributeDefinitionScopeRepository);
        return new SpecificationPredicateCompiler(new ConfigurationResourceRegistry(resolver));
    }

    private List<Producer> execute(Specification<Producer> specification) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Producer> query = cb.createQuery(Producer.class);
        Root<Producer> root = query.from(Producer.class);
        query.where(specification.toPredicate(root, query, cb));
        return entityManager.createQuery(query).getResultList();
    }

    private Organisation persistOrganisation(String name) {
        Organisation org = new Organisation();
        org.setName(name);
        entityManager.persist(org);
        return org;
    }

    private Producer persistProducer(Organisation org, String name, boolean active) {
        Producer producer = new Producer();
        producer.setName(name);
        producer.setDescription("test producer");
        producer.setOrg(org);
        producer.setActive(active);
        producer.setHost("host.example");
        producer.setPort(BigDecimal.valueOf(443));
        producer.setTls(true);
        producer.setIdpClientId(name + "-client");
        entityManager.persist(producer);
        return producer;
    }

    private AttributeDefinitionScope persistProducerScopedDefinition(String name, String dataType, boolean multi) {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setNamespace("policy");
        definition.setName(name);
        definition.setDescription("test");
        definition.setDataType(dataType);
        definition.setMultiValued(multi);
        definition.setCreatedAt(Timestamp.from(Instant.now()));
        definition.setCreatedBy("test");
        definition = attributeDefinitionRepository.saveAndFlush(definition);

        AttributeScope scope = attributeScopeRepository.findByCode("PRODUCER").orElseThrow();
        AttributeDefinitionScope binding = new AttributeDefinitionScope();
        binding.setAttributeDefinition(definition);
        binding.setAttributeScope(scope);
        binding.setRequired(false);
        binding.setCreatedAt(Timestamp.from(Instant.now()));
        binding.setCreatedBy("test");
        return attributeDefinitionScopeRepository.saveAndFlush(binding);
    }

    private void persistValue(AttributeDefinitionScope binding, Long entityId, String json) {
        AttributeValue value = new AttributeValue();
        value.setAttributeDefinitionScope(binding);
        value.setEntityId(entityId);
        value.setValue(json);
        value.setCreatedAt(Timestamp.from(Instant.now()));
        value.setCreatedBy("test");
        entityManager.persist(value);
    }

    // 3.1 fixed attribute

    @Test
    void fixedAttributeEquality_matchesOnlyExpectedRows() {
        Organisation org = persistOrganisation("org-fixed");
        Producer active = persistProducer(org, "active-producer", true);
        persistProducer(org, "inactive-producer", false);
        entityManager.flush();

        Specification<Producer> spec = compiler()
                .compile(ResourceType.PRODUCER, FilterNode.Comparison.of("active", ComparisonOperator.EQ, true));

        assertThat(execute(spec)).extracting(Producer::getId).containsExactly(active.getId());
    }

    @Test
    void fixedAttributeNeq_matchesOnlyNonMatchingRow() {
        Organisation org = persistOrganisation("org-neq");
        Producer active = persistProducer(org, "active-producer-neq", true);
        persistProducer(org, "inactive-producer-neq", false);
        entityManager.flush();

        Specification<Producer> spec = compiler()
                .compile(ResourceType.PRODUCER, FilterNode.Comparison.of("active", ComparisonOperator.NEQ, false));

        assertThat(execute(spec)).extracting(Producer::getId).containsExactly(active.getId());
    }

    @Test
    void fixedAttributeIn_matchesAnyListedId() {
        Organisation org = persistOrganisation("org-in");
        Producer first = persistProducer(org, "in-producer-1", true);
        Producer second = persistProducer(org, "in-producer-2", true);
        persistProducer(org, "in-producer-3", true);
        entityManager.flush();

        Specification<Producer> spec = compiler()
                .compile(
                        ResourceType.PRODUCER,
                        new FilterNode.Comparison("id", ComparisonOperator.IN, List.of(first.getId(), second.getId())));

        assertThat(execute(spec)).extracting(Producer::getId).containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void fixedAttributeNotIn_excludesListedIds() {
        Organisation org = persistOrganisation("org-not-in");
        Producer excluded = persistProducer(org, "not-in-producer-1", true);
        Producer kept = persistProducer(org, "not-in-producer-2", true);
        entityManager.flush();

        Specification<Producer> spec = compiler()
                .compile(
                        ResourceType.PRODUCER,
                        new FilterNode.Comparison("id", ComparisonOperator.NOT_IN, List.of(excluded.getId())));

        assertThat(execute(spec))
                .extracting(Producer::getId)
                .contains(kept.getId())
                .doesNotContain(excluded.getId());
    }

    @Test
    void fixedAttributeRangeOperators_compareDecimalColumn() {
        Organisation org = persistOrganisation("org-range");
        Producer low = persistProducer(org, "range-producer-low", true);
        low.setPort(BigDecimal.valueOf(100));
        Producer high = persistProducer(org, "range-producer-high", true);
        high.setPort(BigDecimal.valueOf(900));
        entityManager.flush();

        assertThat(execute(compiler()
                        .compile(ResourceType.PRODUCER, FilterNode.Comparison.of("port", ComparisonOperator.LT, 500))))
                .extracting(Producer::getId)
                .containsExactly(low.getId());
        assertThat(execute(compiler()
                        .compile(ResourceType.PRODUCER, FilterNode.Comparison.of("port", ComparisonOperator.LTE, 100))))
                .extracting(Producer::getId)
                .containsExactly(low.getId());
        assertThat(execute(compiler()
                        .compile(ResourceType.PRODUCER, FilterNode.Comparison.of("port", ComparisonOperator.GTE, 900))))
                .extracting(Producer::getId)
                .containsExactly(high.getId());
    }

    @Test
    void fixedAttributeContains_matchesCaseInsensitiveSubstring() {
        Organisation org = persistOrganisation("org-contains");
        Producer matching = persistProducer(org, "alpha-producer", true);
        persistProducer(org, "beta-producer", true);
        entityManager.flush();

        Specification<Producer> spec = compiler()
                .compile(ResourceType.PRODUCER, FilterNode.Comparison.of("name", ComparisonOperator.CONTAINS, "ALPHA"));

        assertThat(execute(spec)).extracting(Producer::getId).containsExactly(matching.getId());
    }

    // 3.2 dynamic attribute EXISTS subquery

    @Test
    void dynamicAttributeEquality_matchesOnlyRowsWithLiveAttributeValue() {
        Organisation org = persistOrganisation("org-dynamic");
        Producer withAttribute = persistProducer(org, "with-tier", true);
        persistProducer(org, "without-tier", true);
        entityManager.flush();

        AttributeDefinitionScope binding = persistProducerScopedDefinition("risk-tier", "STRING", false);
        persistValue(binding, withAttribute.getId(), "\"gold\"");
        entityManager.flush();

        Specification<Producer> spec = compiler()
                .compile(
                        ResourceType.PRODUCER,
                        FilterNode.Comparison.of("policy.risk-tier", ComparisonOperator.EQ, "gold"));

        assertThat(execute(spec)).extracting(Producer::getId).containsExactly(withAttribute.getId());
    }

    @Test
    void dynamicAttributeExcludesSoftDeletedValue() {
        Organisation org = persistOrganisation("org-soft-deleted");
        Producer producer = persistProducer(org, "soft-deleted-value-producer", true);
        entityManager.flush();

        AttributeDefinitionScope binding = persistProducerScopedDefinition("soft-deleted-tier", "STRING", false);
        AttributeValue value = new AttributeValue();
        value.setAttributeDefinitionScope(binding);
        value.setEntityId(producer.getId());
        value.setValue("\"gold\"");
        value.setIsDeleted(true);
        value.setCreatedAt(Timestamp.from(Instant.now()));
        value.setCreatedBy("test");
        entityManager.persist(value);
        entityManager.flush();

        Specification<Producer> spec = compiler()
                .compile(
                        ResourceType.PRODUCER,
                        FilterNode.Comparison.of("policy.soft-deleted-tier", ComparisonOperator.EQ, "gold"));

        assertThat(execute(spec)).isEmpty();
    }

    // 3.3 per-data_type coercion and cast failure

    @Test
    void dynamicAttributeRangeComparison_castsNumericDataTypeCorrectly() {
        Organisation org = persistOrganisation("org-numeric");
        Producer low = persistProducer(org, "low-priority", true);
        Producer high = persistProducer(org, "high-priority", true);
        entityManager.flush();

        AttributeDefinitionScope binding = persistProducerScopedDefinition("priority", "INTEGER", false);
        persistValue(binding, low.getId(), "5");
        persistValue(binding, high.getId(), "50");
        entityManager.flush();

        Specification<Producer> spec = compiler()
                .compile(ResourceType.PRODUCER, FilterNode.Comparison.of("policy.priority", ComparisonOperator.GT, 10));

        assertThat(execute(spec)).extracting(Producer::getId).containsExactly(high.getId());
    }

    @Test
    void dynamicAttributeBooleanCast_matchesStoredBooleanValue() {
        Organisation org = persistOrganisation("org-boolean");
        Producer producer = persistProducer(org, "flagged-producer", true);
        entityManager.flush();

        AttributeDefinitionScope binding = persistProducerScopedDefinition("flagged", "BOOLEAN", false);
        persistValue(binding, producer.getId(), "true");
        entityManager.flush();

        Specification<Producer> spec = compiler()
                .compile(
                        ResourceType.PRODUCER, FilterNode.Comparison.of("policy.flagged", ComparisonOperator.EQ, true));

        assertThat(execute(spec)).extracting(Producer::getId).containsExactly(producer.getId());
    }

    @Test
    void dynamicAttributeCastFailure_throwsRatherThanReturningWrongResult() {
        Organisation org = persistOrganisation("org-cast-failure");
        Producer producer = persistProducer(org, "bad-numeric-value-producer", true);
        entityManager.flush();

        AttributeDefinitionScope binding = persistProducerScopedDefinition("broken-priority", "INTEGER", false);
        // Stored value cannot be cast to INTEGER at query time - nothing in the schema enforces
        // that attribute_value.value matches its definition's declared data_type (see design.md).
        persistValue(binding, producer.getId(), "\"not-a-number\"");
        entityManager.flush();

        Specification<Producer> spec = compiler()
                .compile(
                        ResourceType.PRODUCER,
                        FilterNode.Comparison.of("policy.broken-priority", ComparisonOperator.GT, 1));

        assertThatThrownBy(() -> execute(spec)).isInstanceOf(PersistenceException.class);
    }

    // 3.4 nested groups mixing fixed and dynamic

    @Test
    void groupAnd_combinesFixedAndDynamicComparisons() {
        Organisation org = persistOrganisation("org-and");
        Producer matches = persistProducer(org, "matches-both", true);
        Producer failsFixed = persistProducer(org, "fails-fixed", false);
        entityManager.flush();

        AttributeDefinitionScope binding = persistProducerScopedDefinition("and-tier", "STRING", false);
        persistValue(binding, matches.getId(), "\"gold\"");
        persistValue(binding, failsFixed.getId(), "\"gold\"");
        entityManager.flush();

        Specification<Producer> spec = compiler()
                .compile(
                        ResourceType.PRODUCER,
                        FilterNode.Group.and(List.of(
                                FilterNode.Comparison.of("active", ComparisonOperator.EQ, true),
                                FilterNode.Comparison.of("policy.and-tier", ComparisonOperator.EQ, "gold"))));

        assertThat(execute(spec)).extracting(Producer::getId).containsExactly(matches.getId());
    }

    @Test
    void groupOr_combinesFixedAndDynamicComparisons() {
        Organisation org = persistOrganisation("org-or");
        Producer matchesFixed = persistProducer(org, "matches-fixed-only", true);
        Producer matchesDynamic = persistProducer(org, "matches-dynamic-only", false);
        persistProducer(org, "matches-neither", false);
        entityManager.flush();

        AttributeDefinitionScope binding = persistProducerScopedDefinition("or-tier", "STRING", false);
        persistValue(binding, matchesDynamic.getId(), "\"gold\"");
        entityManager.flush();

        Specification<Producer> spec = compiler()
                .compile(
                        ResourceType.PRODUCER,
                        FilterNode.Group.or(List.of(
                                FilterNode.Comparison.of("active", ComparisonOperator.EQ, true),
                                FilterNode.Comparison.of("policy.or-tier", ComparisonOperator.EQ, "gold"))));

        assertThat(execute(spec))
                .extracting(Producer::getId)
                .containsExactlyInAnyOrder(matchesFixed.getId(), matchesDynamic.getId());
    }

    // 3.5 rejections

    @Test
    void unknownAttribute_rejectedWithRequestOriginAndNoInternalLeak() {
        // compile() only returns a lazy Specification; resolution happens when the predicate is built.
        Specification<Producer> spec =
                compiler().compile(ResourceType.PRODUCER, FilterNode.Comparison.of("nope", ComparisonOperator.EQ, "x"));

        assertThatThrownBy(() -> execute(spec))
                .isInstanceOf(FilterCompilationException.class)
                .satisfies(e -> {
                    FilterCompilationException fce = (FilterCompilationException) e;
                    assertThat(fce.origin()).isEqualTo(Origin.REQUEST);
                    assertThat(fce.getMessage()).contains("'nope'");
                    assertThat(fce.getMessage())
                            .doesNotContain("attribute_value")
                            .doesNotContain("attribute_definition");
                });
    }

    @Test
    void operatorUnsupportedForType_rejected() {
        Specification<Producer> spec = compiler()
                .compile(ResourceType.PRODUCER, FilterNode.Comparison.of("active", ComparisonOperator.CONTAINS, "x"));

        assertThatThrownBy(() -> execute(spec))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(Origin.REQUEST);
    }

    @Test
    void wrongArity_rejectedForSingleValueOperator() {
        Specification<Producer> spec = compiler()
                .compile(
                        ResourceType.PRODUCER,
                        new FilterNode.Comparison("active", ComparisonOperator.EQ, List.of(true, false)));

        assertThatThrownBy(() -> execute(spec))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(Origin.REQUEST);
    }

    @Test
    void wrongOperandType_rejected() {
        Specification<Producer> spec = compiler()
                .compile(
                        ResourceType.PRODUCER, FilterNode.Comparison.of("port", ComparisonOperator.GT, "not-a-number"));

        assertThatThrownBy(() -> execute(spec))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(Origin.REQUEST);
    }

    // Regression tests for the multi-valued NEQ/NOT_IN semantics bug fixed after code review:
    // each Comparison against a dynamic attribute compiles to one EXISTS subquery, so NEQ/NOT_IN
    // on a multi-valued attribute would mean "EXISTS a value that doesn't match" (true as soon
    // as any other value is present) rather than the "does not have this value" a caller would
    // expect - so those operators are rejected outright for multi-valued attributes, while
    // EQ/IN ("has a matching value") keep their unambiguous EXISTS semantics.

    @Test
    void multiValuedAttribute_rejectsNeq() {
        persistProducerScopedDefinition("tags", "STRING", true);
        Specification<Producer> spec = compiler()
                .compile(ResourceType.PRODUCER, FilterNode.Comparison.of("policy.tags", ComparisonOperator.NEQ, "red"));

        assertThatThrownBy(() -> execute(spec))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(Origin.REQUEST);
    }

    @Test
    void multiValuedAttribute_rejectsNotIn() {
        persistProducerScopedDefinition("tags-not-in", "STRING", true);
        Specification<Producer> spec = compiler()
                .compile(
                        ResourceType.PRODUCER,
                        new FilterNode.Comparison("policy.tags-not-in", ComparisonOperator.NOT_IN, List.of("red")));

        assertThatThrownBy(() -> execute(spec))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(Origin.REQUEST);
    }

    @Test
    void multiValuedAttribute_allowsEq_matchingProducerWithThatValueAmongOthers() {
        Organisation org = persistOrganisation("org-multi-eq");
        Producer producer = persistProducer(org, "multi-valued-producer", true);
        entityManager.flush();

        AttributeDefinitionScope binding = persistProducerScopedDefinition("multi-tags", "STRING", true);
        persistValue(binding, producer.getId(), "\"red\"");
        persistValue(binding, producer.getId(), "\"blue\"");
        entityManager.flush();

        Specification<Producer> spec = compiler()
                .compile(
                        ResourceType.PRODUCER,
                        FilterNode.Comparison.of("policy.multi-tags", ComparisonOperator.EQ, "red"));

        assertThat(execute(spec)).extracting(Producer::getId).containsExactly(producer.getId());
    }
}
