/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A database-agnostic predicate tree carrying no SQL, no column names, and no operators beyond
 * {@link ComparisonOperator} - a caller-supplied filter can never express anything the
 * {@code SpecificationPredicateCompiler} cannot bind as a parameter.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = FilterNode.Group.class, name = "group"),
    @JsonSubTypes.Type(value = FilterNode.Comparison.class, name = "comparison"),
    @JsonSubTypes.Type(value = FilterNode.Literal.class, name = "literal")
})
public sealed interface FilterNode {

    /**
     * A conjunction or disjunction of child predicates. An empty {@code AND} is true and an
     * empty {@code OR} is false, matching the identity element of each operation - neither case
     * silently widens a result set.
     */
    record Group(@NotNull Combinator combinator, @NotNull List<FilterNode> nodes) implements FilterNode {

        public Group {
            nodes = nodes == null ? List.of() : List.copyOf(nodes);
        }

        public static Group and(List<FilterNode> nodes) {
            return new Group(Combinator.AND, nodes);
        }

        public static Group or(List<FilterNode> nodes) {
            return new Group(Combinator.OR, nodes);
        }
    }

    /**
     * A comparison of one resource attribute against one or more literal operands.
     *
     * @param attribute logical attribute name, resolved against the resource attribute registry
     *     - never a column name and never interpolated into a query
     * @param operator the comparison to apply
     * @param values operands, still in their JSON representation; coerced to the attribute's
     *     declared type at compile time
     */
    record Comparison(@NotBlank String attribute, @NotNull ComparisonOperator operator, @NotNull List<Object> values)
            implements FilterNode {

        public Comparison {
            values = values == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(values));
        }

        public static Comparison of(String attribute, ComparisonOperator operator, Object... values) {
            return new Comparison(attribute, operator, List.of(values));
        }
    }

    /** A constant predicate. Not emitted by anything in this change; kept for structural parity. */
    record Literal(boolean value) implements FilterNode {
        public static final Literal DENY_ALL = new Literal(false);
        public static final Literal ALLOW_ALL = new Literal(true);
    }
}
