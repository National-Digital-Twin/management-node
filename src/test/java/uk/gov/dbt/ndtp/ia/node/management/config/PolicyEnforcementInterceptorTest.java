/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import uk.gov.dbt.ndtp.ia.node.management.model.jwt.EnhancedPrincipal;
import uk.gov.dbt.ndtp.ia.node.management.service.providers.policy.PolicyDecision;
import uk.gov.dbt.ndtp.ia.node.management.service.providers.policy.PolicyDecisionClient;
import uk.gov.dbt.ndtp.ia.node.management.service.providers.policy.PolicyInput;

class PolicyEnforcementInterceptorTest {

    @Mock
    private PolicyDecisionClient policyDecisionClient;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HandlerMethod handlerMethod;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private PolicyEnforcementInterceptor interceptor;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        interceptor = new PolicyEnforcementInterceptor(policyDecisionClient, new ObjectMapper());
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        closeable.close();
    }

    private void setupAuthentication(String clientId) {
        EnhancedPrincipal principal = new EnhancedPrincipal("subject", clientId);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
    }

    private StringWriter setupResponseWriter() throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);
        return sw;
    }

    @Test
    void noAuthentication_returns403WithoutCallingPdp() throws Exception {
        when(securityContext.getAuthentication()).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/configuration/consumer");
        setupResponseWriter();

        assertThat(interceptor.preHandle(request, response, handlerMethod)).isFalse();
        verify(response).setStatus(403);
        verifyNoInteractions(policyDecisionClient);
    }

    @Test
    void nonEnhancedPrincipal_returns403WithoutCallingPdp() throws Exception {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("plain-string-principal");
        when(request.getRequestURI()).thenReturn("/api/v1/configuration/consumer");
        setupResponseWriter();

        assertThat(interceptor.preHandle(request, response, handlerMethod)).isFalse();
        verify(response).setStatus(403);
        verifyNoInteractions(policyDecisionClient);
    }

    @Test
    void emptyClientId_returns403WithoutCallingPdp() throws Exception {
        setupAuthentication("");
        when(request.getRequestURI()).thenReturn("/api/v1/configuration/consumer");
        setupResponseWriter();

        assertThat(interceptor.preHandle(request, response, handlerMethod)).isFalse();
        verify(response).setStatus(403);
        verifyNoInteractions(policyDecisionClient);
    }

    @Test
    void pdpAllows_returnsTrue() throws Exception {
        setupAuthentication("client-1");
        when(request.getRequestURI()).thenReturn("/api/v1/configuration/producer");
        when(request.getMethod()).thenReturn("GET");
        when(policyDecisionClient.evaluate(any(PolicyInput.class))).thenReturn(PolicyDecision.ALLOW);

        assertThat(interceptor.preHandle(request, response, handlerMethod)).isTrue();
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void pdpDenies_returns403() throws Exception {
        setupAuthentication("client-1");
        when(request.getRequestURI()).thenReturn("/api/v1/configuration/producer");
        when(request.getMethod()).thenReturn("GET");
        when(policyDecisionClient.evaluate(any(PolicyInput.class))).thenReturn(PolicyDecision.DENY);
        StringWriter sw = setupResponseWriter();

        assertThat(interceptor.preHandle(request, response, handlerMethod)).isFalse();
        verify(response).setStatus(403);
        assertThat(sw.toString()).contains("403").contains("Access denied by policy");
    }

    @Test
    void policyInput_includesClientResourceAndAction() throws Exception {
        setupAuthentication("client-1");
        when(request.getRequestURI()).thenReturn("/api/v1/configuration/consumer");
        when(request.getMethod()).thenReturn("GET");
        when(policyDecisionClient.evaluate(any(PolicyInput.class))).thenReturn(PolicyDecision.ALLOW);

        interceptor.preHandle(request, response, handlerMethod);

        verify(policyDecisionClient)
                .evaluate(new PolicyInput("client-1", null, "/api/v1/configuration/consumer", "GET"));
    }
}
