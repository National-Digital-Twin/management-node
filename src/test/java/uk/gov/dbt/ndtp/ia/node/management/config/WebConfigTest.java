/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @Mock
    private CertificateValidationInterceptor certificateValidationInterceptor;

    @Mock
    private PolicyEnforcementInterceptor policyEnforcementInterceptor;

    @Mock
    private InterceptorRegistry registry;

    @Mock
    private InterceptorRegistration registration;

    private WebConfig config;

    @BeforeEach
    void setUp() {
        when(registry.addInterceptor(any())).thenReturn(registration);
        when(registration.addPathPatterns(any(String.class))).thenReturn(registration);
        lenient().when(registration.addPathPatterns(anyList())).thenReturn(registration);
    }

    private WebConfig configWithProtectedPaths(List<String> protectedPaths) {
        OpaProperties opaProperties = new OpaProperties(
                "https://opa.example.internal",
                "/v1/data/management_node/allow",
                Duration.ofSeconds(2),
                Duration.ofSeconds(3),
                protectedPaths);
        return new WebConfig(certificateValidationInterceptor, policyEnforcementInterceptor, opaProperties);
    }

    @Test
    void certificateEndpoints_excludedFromCertificateValidation() {
        config = configWithProtectedPaths(List.of("/api/v1/configuration/**"));
        config.addInterceptors(registry);

        verify(registry).addInterceptor(certificateValidationInterceptor);
        verify(registration).addPathPatterns("/api/**");
        verify(registration).excludePathPatterns("/api/v1/certificate/**");
    }

    @Test
    void configurationEndpoints_registeredForPolicyEnforcement() {
        config = configWithProtectedPaths(List.of("/api/v1/configuration/**"));
        config.addInterceptors(registry);

        verify(registry).addInterceptor(policyEnforcementInterceptor);
        verify(registration).addPathPatterns(List.of("/api/v1/configuration/**"));
    }

    @Test
    void emptyProtectedPaths_doesNotRegisterPolicyEnforcementInterceptor() {
        config = configWithProtectedPaths(List.of());
        config.addInterceptors(registry);

        verify(registry, never()).addInterceptor(policyEnforcementInterceptor);
    }
}
