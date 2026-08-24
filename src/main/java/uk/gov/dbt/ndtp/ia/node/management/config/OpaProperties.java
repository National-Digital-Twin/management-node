/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Connection settings for the OPA Policy Decision Point (PDP).
 *
 * @param url base URL of the OPA server (e.g. http://localhost:8181)
 * @param decisionPath path appended to the base URL for the decision query (e.g. /v1/data/management_node/allow)
 * @param connectTimeout maximum time to wait to establish a connection
 * @param readTimeout maximum time to wait for a response
 */
@ConfigurationProperties(prefix = "application.opa")
public record OpaProperties(String url, String decisionPath, Duration connectTimeout, Duration readTimeout) {}
