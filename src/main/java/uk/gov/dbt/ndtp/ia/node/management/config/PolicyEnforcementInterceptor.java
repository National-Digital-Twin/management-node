/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.dbt.ndtp.ia.node.management.exception.ErrorResponse;
import uk.gov.dbt.ndtp.ia.node.management.model.jwt.EnhancedPrincipal;
import uk.gov.dbt.ndtp.ia.node.management.service.providers.policy.PolicyDecision;
import uk.gov.dbt.ndtp.ia.node.management.service.providers.policy.PolicyDecisionClient;
import uk.gov.dbt.ndtp.ia.node.management.service.providers.policy.PolicyInput;

/**
 * Policy Enforcement Point: intercepts requests to policy-aware APIs, enriches them
 * with identity and resource attributes, and enforces the PDP (OPA) allow/deny decision.
 * Runs after authentication has already populated the {@link SecurityContextHolder}.
 */
@Component
@Slf4j
public class PolicyEnforcementInterceptor implements HandlerInterceptor {

    private final PolicyDecisionClient policyDecisionClient;
    private final ObjectMapper objectMapper;

    public PolicyEnforcementInterceptor(PolicyDecisionClient policyDecisionClient, ObjectMapper objectMapper) {
        this.policyDecisionClient = policyDecisionClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String clientId = extractClientId();
        if (clientId == null) {
            log.warn("No client ID found for policy-aware request to {}", request.getRequestURI());
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Client ID required");
            return false;
        }

        String resource = request.getRequestURI();
        String action = request.getMethod();
        PolicyInput input = new PolicyInput(clientId, null, resource, action);

        PolicyDecision decision = policyDecisionClient.evaluate(input);

        if (decision == PolicyDecision.DENY) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied by policy");
            return false;
        }

        return true;
    }

    private String extractClientId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof EnhancedPrincipal principal)) {
            return null;
        }
        String clientId = principal.clientId();
        return (clientId == null || clientId.isEmpty()) ? null : clientId;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse errorResponse =
                new ErrorResponse(status, message, UUID.randomUUID().toString());
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
