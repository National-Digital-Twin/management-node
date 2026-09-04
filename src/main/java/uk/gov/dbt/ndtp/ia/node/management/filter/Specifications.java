/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.filter;

import org.springframework.data.jpa.domain.Specification;

/** Small, reusable {@link Specification} building blocks shared by the config-filtering path. */
public final class Specifications {

    private Specifications() {}

    /** A {@code root.<field> = value} predicate, for a single non-nested entity property. */
    public static <T> Specification<T> fieldEquals(String field, Object value) {
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }
}
