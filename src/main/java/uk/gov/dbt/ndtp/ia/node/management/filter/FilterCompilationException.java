/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter;

/**
 * Raised when a {@link FilterNode} cannot be compiled into a database query predicate.
 *
 * <p>{@link Origin} decides the HTTP response: a malformed caller filter is a client error,
 * whereas an attribute definition this system's own configuration cannot honour (an
 * unrecognised {@code data_type}, or a stored value that fails to cast to its declared type) is
 * an internal fault. The latter must never degrade into "apply what could be understood" - a
 * partially applied filter is indistinguishable from a data leak - so both cases abort the
 * request rather than return a partial or unfiltered result.
 */
public class FilterCompilationException extends RuntimeException {

    /** Which trust domain produced the offending predicate. */
    public enum Origin {
        /** A caller-supplied filter is malformed, unknown, or type-mismatched. Maps to 400. */
        REQUEST,
        /** This system's own attribute configuration or stored data is inconsistent. Maps to 500. */
        POLICY
    }

    private final Origin origin;

    public FilterCompilationException(Origin origin, String message) {
        super(message);
        this.origin = origin;
    }

    public Origin origin() {
        return origin;
    }
}
