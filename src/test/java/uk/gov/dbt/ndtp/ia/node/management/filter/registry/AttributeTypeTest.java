/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import uk.gov.dbt.ndtp.ia.node.management.filter.ComparisonOperator;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException.Origin;

class AttributeTypeTest {

    @Test
    void string_coercesStringOnly() {
        assertThat(AttributeType.STRING.coerce("abc", "name")).isEqualTo("abc");
        assertThatThrownBy(() -> AttributeType.STRING.coerce(1, "name"))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(Origin.REQUEST);
    }

    @Test
    void long_coercesVariousNumericRepresentations() {
        assertThat(AttributeType.LONG.coerce(42, "id")).isEqualTo(42L);
        assertThat(AttributeType.LONG.coerce("42", "id")).isEqualTo(42L);
        assertThat(AttributeType.LONG.coerce(42L, "id")).isEqualTo(42L);
    }

    @Test
    void long_rejectsNonNumericString() {
        assertThatThrownBy(() -> AttributeType.LONG.coerce("not-a-number", "id"))
                .isInstanceOf(FilterCompilationException.class);
    }

    @Test
    void integer_rejectsOutOfRangeValue() {
        assertThatThrownBy(() -> AttributeType.INTEGER.coerce(Long.MAX_VALUE, "port"))
                .isInstanceOf(FilterCompilationException.class);
    }

    @Test
    void decimal_coercesNumericAndStringRepresentations() {
        assertThat(AttributeType.DECIMAL.coerce("1.50", "port")).isEqualTo(new BigDecimal("1.50"));
        assertThat(AttributeType.DECIMAL.coerce(2, "port")).isEqualTo(BigDecimal.valueOf(2));
    }

    @Test
    void boolean_coercesBooleanAndStringRepresentations() {
        assertThat(AttributeType.BOOLEAN.coerce(true, "active")).isEqualTo(true);
        assertThat(AttributeType.BOOLEAN.coerce("false", "active")).isEqualTo(false);
        assertThatThrownBy(() -> AttributeType.BOOLEAN.coerce("maybe", "active"))
                .isInstanceOf(FilterCompilationException.class);
    }

    @Test
    void coerce_rejectsNullOperand() {
        assertThatThrownBy(() -> AttributeType.STRING.coerce(null, "name"))
                .isInstanceOf(FilterCompilationException.class)
                .hasMessageContaining("does not accept a null operand");
    }

    @Test
    void supports_reflectsPerTypeOperatorDomain() {
        assertThat(AttributeType.BOOLEAN.supports(ComparisonOperator.EQ)).isTrue();
        assertThat(AttributeType.BOOLEAN.supports(ComparisonOperator.CONTAINS)).isFalse();
        assertThat(AttributeType.STRING.supports(ComparisonOperator.CONTAINS)).isTrue();
        assertThat(AttributeType.LONG.supports(ComparisonOperator.GT)).isTrue();
    }

    @Test
    void fromDataType_resolvesKnownTypesCaseInsensitively() {
        assertThat(AttributeType.fromDataType("string")).isEqualTo(AttributeType.STRING);
        assertThat(AttributeType.fromDataType("DECIMAL")).isEqualTo(AttributeType.DECIMAL);
    }

    @Test
    void fromDataType_rejectsUnknownTypeAsPolicyOrigin() {
        assertThatThrownBy(() -> AttributeType.fromDataType("XML"))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(Origin.POLICY);
    }
}
