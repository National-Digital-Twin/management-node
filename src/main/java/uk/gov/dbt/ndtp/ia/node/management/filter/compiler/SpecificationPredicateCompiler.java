/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter.compiler;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaExpression;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import uk.gov.dbt.ndtp.ia.node.management.filter.ComparisonOperator;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException.Origin;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterNode;
import uk.gov.dbt.ndtp.ia.node.management.filter.registry.AttributeType;
import uk.gov.dbt.ndtp.ia.node.management.filter.registry.ConfigurationResourceRegistry;
import uk.gov.dbt.ndtp.ia.node.management.filter.registry.ResourceAttribute;
import uk.gov.dbt.ndtp.ia.node.management.filter.registry.ResourceType;
import uk.gov.dbt.ndtp.ia.node.management.persistency.entity.AttributeValue;

/**
 * Compiles a validated {@link FilterNode} into a Spring Data JPA {@link Specification}. A fixed
 * attribute becomes a direct {@code CriteriaBuilder} predicate on the entity path; a dynamic
 * attribute becomes a correlated {@code EXISTS} subquery against {@code attribute_value},
 * scoped by the attribute's already-resolved {@code attribute_definition_scope.id} - never a
 * caller-supplied string - with the operand cast to the attribute's declared {@code data_type}.
 */
@Component
public class SpecificationPredicateCompiler {

    private final ConfigurationResourceRegistry registry;

    public SpecificationPredicateCompiler(ConfigurationResourceRegistry registry) {
        this.registry = registry;
    }

    public <T> Specification<T> compile(ResourceType resourceType, FilterNode filter) {
        return (root, query, cb) -> toPredicate(filter, resourceType, root, query, cb);
    }

    private Predicate toPredicate(
            FilterNode node, ResourceType resourceType, Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return switch (node) {
            case FilterNode.Group group -> groupPredicate(group, resourceType, root, query, cb);
            case FilterNode.Comparison comparison -> comparisonPredicate(comparison, resourceType, root, query, cb);
        };
    }

    private Predicate groupPredicate(
            FilterNode.Group group,
            ResourceType resourceType,
            Root<?> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb) {
        List<Predicate> children = group.nodes().stream()
                .map(child -> toPredicate(child, resourceType, root, query, cb))
                .toList();
        return switch (group.combinator()) {
            case AND -> cb.and(children.toArray(new Predicate[0]));
            case OR -> cb.or(children.toArray(new Predicate[0]));
        };
    }

    private Predicate comparisonPredicate(
            FilterNode.Comparison comparison,
            ResourceType resourceType,
            Root<?> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb) {
        ResourceAttribute attribute = registry.resolve(resourceType, comparison.attribute());
        requireSupportedOperator(attribute, comparison.operator(), comparison.attribute());
        List<Object> operands = requireArity(comparison.operator(), comparison.values(), comparison.attribute());
        List<Object> coerced = operands.stream()
                .map(raw -> attribute.type().coerce(raw, comparison.attribute()))
                .toList();

        return switch (attribute) {
            case ResourceAttribute.Fixed fixed -> fixedPredicate(fixed, comparison.operator(), coerced, root, cb);
            case ResourceAttribute.Dynamic dynamic ->
                dynamicPredicate(dynamic, comparison.operator(), coerced, root, query, cb);
        };
    }

    private void requireSupportedOperator(ResourceAttribute attribute, ComparisonOperator operator, String name) {
        if (!attribute.type().supports(operator)) {
            throw new FilterCompilationException(
                    Origin.REQUEST,
                    "Operator '" + operator.wireName() + "' is not supported for attribute '" + name + "'");
        }
        // A multi-valued attribute compiles to one EXISTS subquery per Comparison (see
        // dynamicPredicate), so only "has a matching value" operators (EQ/IN) have unambiguous
        // EXISTS semantics. NEQ/NOT_IN would mean "EXISTS a value that doesn't match", which is
        // true as soon as ANY other value is present - not "does not have this value" as a
        // caller would reasonably expect - so they're rejected here rather than silently
        // compiled to the wrong predicate.
        boolean isExistsSafe = operator == ComparisonOperator.EQ || operator == ComparisonOperator.IN;
        if (attribute instanceof ResourceAttribute.Dynamic dynamic && dynamic.multiValued() && !isExistsSafe) {
            throw new FilterCompilationException(
                    Origin.REQUEST,
                    "Operator '" + operator.wireName() + "' is not supported for multi-valued attribute '" + name
                            + "'");
        }
    }

    private List<Object> requireArity(ComparisonOperator operator, List<Object> values, String name) {
        if (operator.arity() == ComparisonOperator.Arity.SINGLE && values.size() != 1) {
            throw new FilterCompilationException(
                    Origin.REQUEST,
                    "Operator '" + operator.wireName() + "' requires exactly one operand for attribute '" + name + "'");
        }
        return values;
    }

    // -----------------------------------------------------------------------------------
    // Fixed attributes
    // -----------------------------------------------------------------------------------

    private Predicate fixedPredicate(
            ResourceAttribute.Fixed fixed,
            ComparisonOperator operator,
            List<Object> values,
            Root<?> root,
            CriteriaBuilder cb) {
        Path<?> path = resolvePath(root, fixed.jpaPath());
        return buildComparison(cb, path, operator, values);
    }

    private static Path<?> resolvePath(Root<?> root, String dottedPath) {
        Path<?> path = root;
        for (String segment : dottedPath.split("\\.")) {
            path = path.get(segment);
        }
        return path;
    }

    // -----------------------------------------------------------------------------------
    // Dynamic attributes
    // -----------------------------------------------------------------------------------

    private Predicate dynamicPredicate(
            ResourceAttribute.Dynamic dynamic,
            ComparisonOperator operator,
            List<Object> values,
            Root<?> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb) {
        HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<AttributeValue> attributeValue = subquery.from(AttributeValue.class);
        subquery.select(cb.literal(1L));

        // Hibernate's own Path implementation also implements JpaExpression; the JPA-standard
        // Path/Expression interfaces returned by Root#get don't expose that statically.
        @SuppressWarnings("unchecked")
        JpaExpression<String> valuePath = (JpaExpression<String>) attributeValue.<String>get("value");
        JpaExpression<String> rawText = hcb.cast(valuePath, String.class);
        var castExpression = castTo(hcb, rawText, dynamic.type());

        List<Predicate> conditions = new ArrayList<>();
        conditions.add(cb.equal(
                attributeValue.get("attributeDefinitionScope").get("id"), dynamic.attributeDefinitionScopeId()));
        conditions.add(cb.equal(attributeValue.get("entityId"), root.get("id")));
        conditions.add(cb.isFalse(attributeValue.get("isDeleted")));
        conditions.add(buildComparison(cb, castExpression, operator, values));

        subquery.where(cb.and(conditions.toArray(new Predicate[0])));
        return cb.exists(subquery);
    }

    /**
     * {@code attribute_value.value::text} renders a JSON string with its surrounding quotes
     * (e.g. {@code "gold"}) but a JSON number/boolean without them (e.g. {@code 42}, {@code
     * true}) - so only the {@code STRING} case needs unquoting before use as a plain value.
     */
    private static JpaExpression<?> castTo(
            HibernateCriteriaBuilder hcb, JpaExpression<String> rawText, AttributeType type) {
        return switch (type) {
            case STRING -> hcb.function("btrim", String.class, rawText, hcb.literal("\""));
            case BOOLEAN -> hcb.cast(rawText, Boolean.class);
            case INTEGER -> hcb.cast(rawText, Integer.class);
            case LONG -> hcb.cast(rawText, Long.class);
            case DECIMAL -> hcb.cast(rawText, BigDecimal.class);
        };
    }

    // -----------------------------------------------------------------------------------
    // Shared comparison building
    // -----------------------------------------------------------------------------------

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Predicate buildComparison(
            CriteriaBuilder cb, Expression<?> expression, ComparisonOperator operator, List<Object> values) {
        return switch (operator) {
            case EQ -> cb.equal(expression, values.get(0));
            case NEQ -> cb.notEqual(expression, values.get(0));
            case IN -> ((Expression) expression).in(values);
            case NOT_IN -> cb.not(((Expression) expression).in(values));
            case LT -> cb.lessThan((Expression<Comparable>) expression, (Comparable) values.get(0));
            case LTE -> cb.lessThanOrEqualTo((Expression<Comparable>) expression, (Comparable) values.get(0));
            case GT -> cb.greaterThan((Expression<Comparable>) expression, (Comparable) values.get(0));
            case GTE -> cb.greaterThanOrEqualTo((Expression<Comparable>) expression, (Comparable) values.get(0));
            case CONTAINS -> containsPredicate(cb, (Expression<String>) expression, (String) values.get(0));
        };
    }

    private static final char LIKE_ESCAPE = '\\';

    private static Predicate containsPredicate(CriteriaBuilder cb, Expression<String> expression, String needle) {
        String escaped = needle.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return cb.like(cb.lower(expression), "%" + escaped.toLowerCase(Locale.ROOT) + "%", LIKE_ESCAPE);
    }
}
