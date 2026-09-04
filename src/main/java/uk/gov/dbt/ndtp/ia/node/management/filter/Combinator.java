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

/** How the children of a {@link FilterNode.Group} combine. */
public enum Combinator {
    AND("and"),
    OR("or");

    private final String wireName;

    Combinator(String wireName) {
        this.wireName = wireName;
    }

    @JsonValue
    public String wireName() {
        return wireName;
    }

    @JsonCreator
    public static Combinator fromWireName(String value) {
        String normalised = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(combinator -> combinator.wireName.equals(normalised))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unsupported combinator '" + value + "'; supported: [and, or]"));
    }
}
