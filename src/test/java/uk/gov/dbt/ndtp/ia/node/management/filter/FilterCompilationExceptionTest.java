/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException.Origin;

class FilterCompilationExceptionTest {

    @Test
    void carriesOriginAndMessage() {
        FilterCompilationException exception = new FilterCompilationException(Origin.REQUEST, "unknown attribute");

        assertThat(exception.origin()).isEqualTo(Origin.REQUEST);
        assertThat(exception.getMessage()).isEqualTo("unknown attribute");
    }

    @Test
    void policyOriginDistinctFromRequestOrigin() {
        FilterCompilationException exception = new FilterCompilationException(Origin.POLICY, "bad data_type");

        assertThat(exception.origin()).isEqualTo(Origin.POLICY);
    }
}
