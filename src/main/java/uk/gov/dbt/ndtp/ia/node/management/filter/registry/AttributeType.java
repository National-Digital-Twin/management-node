/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter.registry;

import static uk.gov.dbt.ndtp.ia.node.management.filter.ComparisonOperator.CONTAINS;
import static uk.gov.dbt.ndtp.ia.node.management.filter.ComparisonOperator.EQ;
import static uk.gov.dbt.ndtp.ia.node.management.filter.ComparisonOperator.GT;
import static uk.gov.dbt.ndtp.ia.node.management.filter.ComparisonOperator.GTE;
import static uk.gov.dbt.ndtp.ia.node.management.filter.ComparisonOperator.IN;
import static uk.gov.dbt.ndtp.ia.node.management.filter.ComparisonOperator.LT;
import static uk.gov.dbt.ndtp.ia.node.management.filter.ComparisonOperator.LTE;
import static uk.gov.dbt.ndtp.ia.node.management.filter.ComparisonOperator.NEQ;
import static uk.gov.dbt.ndtp.ia.node.management.filter.ComparisonOperator.NOT_IN;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;
import uk.gov.dbt.ndtp.ia.node.management.filter.ComparisonOperator;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException.Origin;

/**
 * The value domain of a filterable resource attribute, fixed or dynamic. Declares which
 * operators are meaningful for the attribute - so a caller cannot ask for a substring match on a
 * boolean, or an ordering comparison on an opaque identifier - and converts a JSON operand into
 * the exact Java type the query needs. An operand that cannot be converted is rejected rather
 * than passed to the query, because that is the point at which a query would otherwise start
 * matching the wrong rows.
 */
public enum AttributeType {
    STRING(Set.of(EQ, NEQ, IN, NOT_IN, CONTAINS)) {
        @Override
        Object convert(Object raw) {
            if (raw instanceof String text) {
                return text;
            }
            throw typeError(raw, "a string");
        }
    },

    INTEGER(Set.of(EQ, NEQ, IN, NOT_IN, LT, LTE, GT, GTE)) {
        @Override
        Object convert(Object raw) {
            long value = toLong(raw, "a whole number");
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                throw typeError(raw, "a 32-bit whole number");
            }
            return (int) value;
        }
    },

    LONG(Set.of(EQ, NEQ, IN, NOT_IN, LT, LTE, GT, GTE)) {
        @Override
        Object convert(Object raw) {
            return toLong(raw, "a whole number");
        }
    },

    DECIMAL(Set.of(EQ, NEQ, IN, NOT_IN, LT, LTE, GT, GTE)) {
        @Override
        Object convert(Object raw) {
            try {
                return switch (raw) {
                    case BigDecimal decimal -> decimal;
                    case Integer integer -> BigDecimal.valueOf(integer.longValue());
                    case Long value -> BigDecimal.valueOf(value);
                    case Double value -> BigDecimal.valueOf(value);
                    case Float value -> BigDecimal.valueOf(value.doubleValue());
                    case String text -> new BigDecimal(text.trim());
                    case null, default -> throw typeError(raw, "a decimal number");
                };
            } catch (NumberFormatException e) {
                throw typeError(raw, "a decimal number");
            }
        }
    },

    BOOLEAN(Set.of(EQ, NEQ)) {
        @Override
        Object convert(Object raw) {
            if (raw instanceof Boolean value) {
                return value;
            }
            if (raw instanceof String text) {
                String normalised = text.trim().toLowerCase(Locale.ROOT);
                if ("true".equals(normalised)) {
                    return Boolean.TRUE;
                }
                if ("false".equals(normalised)) {
                    return Boolean.FALSE;
                }
            }
            throw typeError(raw, "a boolean");
        }
    };

    private final Set<ComparisonOperator> supportedOperators;

    AttributeType(Set<ComparisonOperator> supportedOperators) {
        this.supportedOperators = supportedOperators;
    }

    public Set<ComparisonOperator> supportedOperators() {
        return supportedOperators;
    }

    public boolean supports(ComparisonOperator operator) {
        return supportedOperators.contains(operator);
    }

    /**
     * Converts a JSON operand to the type the compiled predicate needs.
     *
     * @throws FilterCompilationException(REQUEST) if the operand is null or not convertible
     */
    public Object coerce(Object raw, String attributeName) {
        if (raw == null) {
            throw new FilterCompilationException(
                    Origin.REQUEST, "Attribute '" + attributeName + "' does not accept a null operand");
        }
        try {
            return convert(raw);
        } catch (IllegalArgumentException e) {
            throw new FilterCompilationException(
                    Origin.REQUEST, "Attribute '" + attributeName + "' expects " + e.getMessage());
        }
    }

    abstract Object convert(Object raw);

    /**
     * Resolves the closed type domain an {@code attribute_definition.data_type} value declares.
     *
     * @throws FilterCompilationException(POLICY) if the value is not one of this enum's names -
     *     a configuration/data defect, not a caller error, since the caller never supplies this
     *     value
     */
    public static AttributeType fromDataType(String dataType) {
        if (dataType != null) {
            for (AttributeType type : values()) {
                if (type.name().equalsIgnoreCase(dataType.trim())) {
                    return type;
                }
            }
        }
        throw new FilterCompilationException(
                Origin.POLICY, "Attribute definition declares unsupported data_type '" + dataType + "'");
    }

    private static long toLong(Object raw, String expectation) {
        return switch (raw) {
            case Integer value -> value.longValue();
            case Long value -> value;
            case Short value -> value.longValue();
            case Byte value -> value.longValue();
            case BigDecimal value -> exactLong(value, expectation);
            case Double value -> exactLong(BigDecimal.valueOf(value), expectation);
            case Float value -> exactLong(BigDecimal.valueOf(value.doubleValue()), expectation);
            case String text -> parseLong(text, expectation);
            case null, default -> throw typeError(raw, expectation);
        };
    }

    private static long exactLong(BigDecimal value, String expectation) {
        try {
            return value.longValueExact();
        } catch (ArithmeticException e) {
            throw typeError(value, expectation);
        }
    }

    private static long parseLong(String text, String expectation) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            throw typeError(text, expectation);
        }
    }

    /**
     * The message carries only the expectation. The rejected operand is deliberately not echoed
     * - reflecting caller input into an error body is an avoidable habit.
     */
    private static IllegalArgumentException typeError(Object raw, String expectation) {
        String actual = raw == null ? "null" : raw.getClass().getSimpleName();
        return new IllegalArgumentException(expectation + " but received " + actual);
    }
}
