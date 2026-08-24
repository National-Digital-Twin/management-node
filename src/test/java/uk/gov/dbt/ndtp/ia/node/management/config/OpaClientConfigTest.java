/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

class OpaClientConfigTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(OpaClientConfig.class);

    @Test
    void createsRestClientBeanFromConfiguredProperties() {
        contextRunner
                .withPropertyValues(
                        "application.opa.url=https://opa.example.internal",
                        "application.opa.decision-path=/v1/data/management_node/allow",
                        "application.opa.connect-timeout=2s",
                        "application.opa.read-timeout=3s")
                .run(context -> assertThat(context).hasSingleBean(RestClient.class));
    }
}
