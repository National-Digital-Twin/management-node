/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Locale;

/**
 * The closed comparison vocabulary a {@link FilterNode.Comparison} may use. Anything a caller
 * names outside this set is rejected rather than interpreted, which is what keeps the
 * translation from a caller filter to a database predicate total and auditable.
 */
public enum ComparisonOperator {
    EQ("eq", Arity.SINGLE),
    NEQ("neq", Arity.SINGLE),
    IN("in", Arity.ANY),
    NOT_IN("not_in", Arity.ANY),
    LT("lt", Arity.SINGLE),
    LTE("lte", Arity.SINGLE),
    GT("gt", Arity.SINGLE),
    GTE("gte", Arity.SINGLE),
    /** Case-insensitive substring match. */
    CONTAINS("contains", Arity.SINGLE);

    /** How many operands the operator accepts. */
    public enum Arity {
        /** Exactly one value. */
        SINGLE,
        /** Zero or more values. */
        ANY
    }

    private final String wireName;
    private final Arity arity;

    ComparisonOperator(String wireName, Arity arity) {
        this.wireName = wireName;
        this.arity = arity;
    }

    @JsonValue
    public String wireName() {
        return wireName;
    }

    public Arity arity() {
        return arity;
    }

    /** {@code true} for operators that require a totally ordered operand type. */
    public boolean isOrdering() {
        return this == LT || this == LTE || this == GT || this == GTE;
    }

    @JsonCreator
    public static ComparisonOperator fromWireName(String value) {
        String normalised = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(operator -> operator.wireName.equals(normalised))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported comparison operator '" + value
                        + "'; supported operators are "
                        + Arrays.stream(values())
                                .map(ComparisonOperator::wireName)
                                .toList()));
    }
}
