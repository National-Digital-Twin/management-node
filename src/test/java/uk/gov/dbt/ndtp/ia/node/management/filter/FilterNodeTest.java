/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import java.util.List;
import org.junit.jupiter.api.Test;

class FilterNodeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deserializesNestedGroupOfComparisons() throws Exception {
        String json =
                """
                {
                  "type": "group",
                  "combinator": "and",
                  "nodes": [
                    { "type": "comparison", "attribute": "active", "operator": "eq", "values": [true] },
                    {
                      "type": "group",
                      "combinator": "or",
                      "nodes": [
                        { "type": "comparison", "attribute": "orgId", "operator": "in", "values": [1, 2, 3] }
                      ]
                    }
                  ]
                }
                """;

        FilterNode node = mapper.readValue(json, FilterNode.class);

        assertThat(node).isInstanceOf(FilterNode.Group.class);
        FilterNode.Group group = (FilterNode.Group) node;
        assertThat(group.combinator()).isEqualTo(Combinator.AND);
        assertThat(group.nodes()).hasSize(2);
        assertThat(group.nodes().get(0)).isInstanceOf(FilterNode.Comparison.class);
        FilterNode.Comparison first = (FilterNode.Comparison) group.nodes().get(0);
        assertThat(first.attribute()).isEqualTo("active");
        assertThat(first.operator()).isEqualTo(ComparisonOperator.EQ);
        assertThat(first.values()).containsExactly(true);

        assertThat(group.nodes().get(1)).isInstanceOf(FilterNode.Group.class);
        FilterNode.Group nested = (FilterNode.Group) group.nodes().get(1);
        assertThat(nested.combinator()).isEqualTo(Combinator.OR);
        FilterNode.Comparison nestedComparison =
                (FilterNode.Comparison) nested.nodes().getFirst();
        assertThat(nestedComparison.values()).containsExactly(1, 2, 3);
    }

    @Test
    void deserializationRejectsUnknownDiscriminator() {
        String json = """
                { "type": "sql_injection", "raw": "1=1" }
                """;

        assertThatThrownBy(() -> mapper.readValue(json, FilterNode.class)).isInstanceOf(InvalidTypeIdException.class);
    }

    @Test
    void deserializationRejectsUnknownOperator() {
        String json =
                """
                { "type": "comparison", "attribute": "active", "operator": "drop_table", "values": [] }
                """;

        assertThatThrownBy(() -> mapper.readValue(json, FilterNode.class))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void comparisonOf_buildsFromVarargs() {
        FilterNode.Comparison comparison = FilterNode.Comparison.of("active", ComparisonOperator.EQ, true);

        assertThat(comparison.attribute()).isEqualTo("active");
        assertThat(comparison.values()).containsExactly(true);
    }

    @Test
    void comparisonValues_defaultToEmptyListWhenNull() {
        FilterNode.Comparison comparison = new FilterNode.Comparison("active", ComparisonOperator.EQ, null);

        assertThat(comparison.values()).isEqualTo(List.of());
    }

    @Test
    void group_defaultsNullNodesToEmptyList() {
        FilterNode.Group group = new FilterNode.Group(Combinator.AND, null);

        assertThat(group.nodes()).isEmpty();
    }
}
