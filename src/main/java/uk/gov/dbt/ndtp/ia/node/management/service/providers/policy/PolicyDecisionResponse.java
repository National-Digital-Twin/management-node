/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.providers.policy;

/**
 * Decision response returned by the PDP (OPA), following OPA's standard REST API
 * shape of {@code {"result": ...}}. Any value other than {@code true} (including a
 * missing or non-boolean result) is treated as DENY by {@link PolicyDecisionClient}.
 *
 * @param result the PDP's decision result
 */
public record PolicyDecisionResponse(Boolean result) {}
