/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @Mock
    private CertificateValidationInterceptor interceptor;

    @Mock
    private InterceptorRegistry registry;

    @Mock
    private InterceptorRegistration registration;

    @Test
    void certificateEndpoints_excludedFromCertificateValidation() {
        when(registry.addInterceptor(any())).thenReturn(registration);
        when(registration.addPathPatterns(any(String.class))).thenReturn(registration);

        WebConfig config = new WebConfig(interceptor);
        config.addInterceptors(registry);

        verify(registration).addPathPatterns("/api/**");
        verify(registration).excludePathPatterns("/api/v1/certificate/**");
    }
}
