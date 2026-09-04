/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ComparisonOperatorTest {

    @Test
    void fromWireName_resolvesEveryDeclaredOperator() {
        for (ComparisonOperator operator : ComparisonOperator.values()) {
            assertThat(ComparisonOperator.fromWireName(operator.wireName())).isEqualTo(operator);
        }
    }

    @Test
    void fromWireName_isCaseAndWhitespaceInsensitive() {
        assertThat(ComparisonOperator.fromWireName(" EQ ")).isEqualTo(ComparisonOperator.EQ);
    }

    @Test
    void fromWireName_rejectsUnknownOperator() {
        assertThatThrownBy(() -> ComparisonOperator.fromWireName("drop_table"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported comparison operator");
    }

    @Test
    void isOrdering_trueOnlyForRangeOperators() {
        assertThat(ComparisonOperator.LT.isOrdering()).isTrue();
        assertThat(ComparisonOperator.LTE.isOrdering()).isTrue();
        assertThat(ComparisonOperator.GT.isOrdering()).isTrue();
        assertThat(ComparisonOperator.GTE.isOrdering()).isTrue();
        assertThat(ComparisonOperator.EQ.isOrdering()).isFalse();
        assertThat(ComparisonOperator.CONTAINS.isOrdering()).isFalse();
    }

    @Test
    void arity_singleForEqualityAndRange_anyForInFamily() {
        assertThat(ComparisonOperator.EQ.arity()).isEqualTo(ComparisonOperator.Arity.SINGLE);
        assertThat(ComparisonOperator.IN.arity()).isEqualTo(ComparisonOperator.Arity.ANY);
        assertThat(ComparisonOperator.NOT_IN.arity()).isEqualTo(ComparisonOperator.Arity.ANY);
    }
}
