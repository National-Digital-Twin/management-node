/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.providers.policy;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Policy attributes describing who is making a request and what they are trying to do,
 * sent to the PDP (OPA) as the {@code input} of a decision request.
 *
 * @param clientId identity of the calling client
 * @param organisation organisation the client belongs to, if known
 * @param resource the requested resource (request URI)
 * @param action the requested action (HTTP method)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PolicyInput(String clientId, String organisation, String resource, String action) {}
