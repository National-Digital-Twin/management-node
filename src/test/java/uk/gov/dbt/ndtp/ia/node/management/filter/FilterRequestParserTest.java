/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException.Origin;

class FilterRequestParserTest {

    private final FilterRequestParser parser = new FilterRequestParser(new ObjectMapper());

    @Test
    void parse_returnsEmptyForNull() {
        assertThat(parser.parse(null)).isEmpty();
    }

    @Test
    void parse_returnsEmptyForBlank() {
        assertThat(parser.parse("   ")).isEmpty();
    }

    @Test
    void parse_parsesValidComparison() {
        String json =
                """
                { "type": "comparison", "attribute": "active", "operator": "eq", "values": [true] }
                """;

        Optional<FilterNode> node = parser.parse(json);

        assertThat(node).isPresent().get().isInstanceOf(FilterNode.Comparison.class);
    }

    @Test
    void parse_rejectsMalformedJson() {
        assertThatThrownBy(() -> parser.parse("{ not json"))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(Origin.REQUEST);
    }

    @Test
    void parse_rejectsFilterOverComparisonCap() throws Exception {
        List<FilterNode> nodes = new ArrayList<>();
        for (int i = 0; i < FilterRequestParser.MAX_COMPARISONS + 1; i++) {
            nodes.add(FilterNode.Comparison.of("active", ComparisonOperator.EQ, true));
        }
        FilterNode.Group group = FilterNode.Group.and(nodes);
        String json = new ObjectMapper().writeValueAsString(group);

        assertThatThrownBy(() -> parser.parse(json))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(Origin.REQUEST);
    }

    @Test
    void parse_acceptsFilterAtComparisonCap() throws Exception {
        List<FilterNode> nodes = new ArrayList<>();
        for (int i = 0; i < FilterRequestParser.MAX_COMPARISONS; i++) {
            nodes.add(FilterNode.Comparison.of("active", ComparisonOperator.EQ, true));
        }
        FilterNode.Group group = FilterNode.Group.and(nodes);
        String json = new ObjectMapper().writeValueAsString(group);

        assertThat(parser.parse(json)).isPresent();
    }

    // Regression tests for the null-validation gap fixed after code review: since this project
    // has no Bean Validation provider on the classpath, readValue() never enforces the
    // @NotNull/@NotBlank on the FilterNode records - a syntactically valid but semantically
    // incomplete filter must be rejected with a proper 400-mapped FilterCompilationException,
    // not left to throw an unhandled NullPointerException deeper in resolution/compilation.

    @Test
    void parse_rejectsBareJsonNullLiteral() {
        assertThatThrownBy(() -> parser.parse("null"))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(Origin.REQUEST);
    }

    @Test
    void parse_rejectsComparisonMissingAttribute() {
        String json =
                """
                { "type": "comparison", "operator": "eq", "values": [true] }
                """;

        assertThatThrownBy(() -> parser.parse(json))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(Origin.REQUEST);
    }

    @Test
    void parse_rejectsComparisonMissingOperator() {
        String json =
                """
                { "type": "comparison", "attribute": "active", "values": [true] }
                """;

        assertThatThrownBy(() -> parser.parse(json))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(Origin.REQUEST);
    }

    @Test
    void parse_rejectsGroupMissingCombinator() {
        String json =
                """
                { "type": "group", "nodes": [
                    { "type": "comparison", "attribute": "active", "operator": "eq", "values": [true] }
                ] }
                """;

        assertThatThrownBy(() -> parser.parse(json))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(Origin.REQUEST);
    }

    @Test
    void parse_rejectsGroupWithNullElementInNodes() {
        String json = """
                { "type": "group", "combinator": "and", "nodes": [null] }
                """;

        assertThatThrownBy(() -> parser.parse(json))
                .isInstanceOf(FilterCompilationException.class)
                .extracting(e -> ((FilterCompilationException) e).origin())
                .isEqualTo(Origin.REQUEST);
    }
}
