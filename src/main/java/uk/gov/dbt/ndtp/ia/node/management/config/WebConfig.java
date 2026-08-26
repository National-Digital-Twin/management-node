/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CertificateValidationInterceptor certificateValidationInterceptor;
    private final PolicyEnforcementInterceptor policyEnforcementInterceptor;
    private final OpaProperties opaProperties;

    public WebConfig(
            CertificateValidationInterceptor certificateValidationInterceptor,
            PolicyEnforcementInterceptor policyEnforcementInterceptor,
            OpaProperties opaProperties) {
        this.certificateValidationInterceptor = certificateValidationInterceptor;
        this.policyEnforcementInterceptor = policyEnforcementInterceptor;
        this.opaProperties = opaProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(certificateValidationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/v1/certificate/**");

        List<String> protectedPaths = opaProperties.protectedPaths();
        if (!protectedPaths.isEmpty()) {
            registry.addInterceptor(policyEnforcementInterceptor).addPathPatterns(protectedPaths);
        }
    }
}
