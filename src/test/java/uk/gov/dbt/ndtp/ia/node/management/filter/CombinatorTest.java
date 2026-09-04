/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CombinatorTest {

    @Test
    void fromWireName_resolvesAndAndOr() {
        assertThat(Combinator.fromWireName("and")).isEqualTo(Combinator.AND);
        assertThat(Combinator.fromWireName("OR")).isEqualTo(Combinator.OR);
    }

    @Test
    void fromWireName_rejectsUnknownCombinator() {
        assertThatThrownBy(() -> Combinator.fromWireName("xor")).isInstanceOf(IllegalArgumentException.class);
    }
}
