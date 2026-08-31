/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.config;

import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Connection settings for the OPA Policy Decision Point (PDP), and the set of API path
 * patterns the Policy Enforcement Point protects.
 *
 * @param url base URL of the OPA server (e.g. http://localhost:8181)
 * @param decisionPath path appended to the base URL for the decision query (e.g. /v1/data/management_node/allow)
 * @param connectTimeout maximum time to wait to establish a connection
 * @param readTimeout maximum time to wait for a response
 * @param protectedPaths Spring MVC path patterns (e.g. /api/v1/configuration/**) that the PEP intercepts.
 *     Required and non-empty: Spring's {@code MappedInterceptor} treats an empty include-pattern list
 *     as "match every path" rather than "match nothing", so this must never be silently absent.
 */
@ConfigurationProperties(prefix = "application.opa")
@Validated
public record OpaProperties(
        String url,
        String decisionPath,
        Duration connectTimeout,
        Duration readTimeout,
        @NotEmpty List<String> protectedPaths) {}
