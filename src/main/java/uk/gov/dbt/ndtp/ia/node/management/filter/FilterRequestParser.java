/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.dbt.ndtp.ia.node.management.filter.FilterCompilationException.Origin;

/**
 * Parses a caller-supplied, JSON-encoded {@code filter} query parameter into a {@link
 * FilterNode}, rejecting malformed JSON and an over-large filter before any attribute
 * resolution or query runs.
 */
@Component
public class FilterRequestParser {

    /** Mirrors {@code opa_poc.api.SearchRequest}'s cap of 20 filters per request. */
    static final int MAX_COMPARISONS = 20;

    private final ObjectMapper objectMapper;

    public FilterRequestParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * @param rawJson the raw {@code filter} query parameter value, or {@code null}/blank for none
     * @return empty when no filter was supplied
     * @throws FilterCompilationException with {@code Origin.REQUEST} if the JSON is malformed or
     *     the filter contains more than {@value #MAX_COMPARISONS} comparisons
     */
    public Optional<FilterNode> parse(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return Optional.empty();
        }
        FilterNode node;
        try {
            node = objectMapper.readValue(rawJson, FilterNode.class);
        } catch (JsonProcessingException e) {
            throw new FilterCompilationException(Origin.REQUEST, "Malformed filter: could not parse JSON");
        }
        // @NotNull/@NotBlank on the FilterNode records are structural documentation only - this
        // project has no Bean Validation provider on the classpath, and readValue never enforces
        // them - so a syntactically valid but semantically incomplete filter (e.g. a comparison
        // with no "attribute", a group with no "combinator", or a bare JSON `null`) must be
        // rejected explicitly here, before it can reach a `switch` on a null enum/record deeper
        // in resolution or compilation and surface as an unhandled 500.
        validate(node);
        int comparisons = countComparisons(node);
        if (comparisons > MAX_COMPARISONS) {
            throw new FilterCompilationException(
                    Origin.REQUEST,
                    "A filter may combine at most " + MAX_COMPARISONS + " comparisons, found " + comparisons);
        }
        return Optional.of(node);
    }

    private static void validate(FilterNode node) {
        if (node == null) {
            throw new FilterCompilationException(Origin.REQUEST, "Filter must not be null");
        }
        switch (node) {
            case FilterNode.Comparison comparison -> {
                if (comparison.attribute() == null || comparison.attribute().isBlank()) {
                    throw new FilterCompilationException(Origin.REQUEST, "A comparison must name an attribute");
                }
                if (comparison.operator() == null) {
                    throw new FilterCompilationException(
                            Origin.REQUEST,
                            "Comparison on attribute '" + comparison.attribute() + "' must name an operator");
                }
            }
            case FilterNode.Group group -> {
                if (group.combinator() == null) {
                    throw new FilterCompilationException(Origin.REQUEST, "A filter group must name a combinator");
                }
                group.nodes().forEach(FilterRequestParser::validate);
            }
        }
    }

    private static int countComparisons(FilterNode node) {
        return switch (node) {
            case FilterNode.Comparison ignored -> 1;
            case FilterNode.Group group ->
                group.nodes().stream()
                        .mapToInt(FilterRequestParser::countComparisons)
                        .sum();
        };
    }
}
