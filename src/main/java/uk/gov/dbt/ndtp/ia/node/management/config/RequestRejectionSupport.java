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
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.dbt.ndtp.ia.node.management.exception.ErrorResponse;
import uk.gov.dbt.ndtp.ia.node.management.model.jwt.EnhancedPrincipal;

/**
 * Shared request-rejection behaviour for {@code HandlerInterceptor}s that gate access
 * on the authenticated client: resolving the client id from the security context and
 * writing a JSON {@link ErrorResponse} for a rejected request.
 */
final class RequestRejectionSupport {

    private static final String ORGANISATION_ID_ATTRIBUTE = "ndtp.organisationId";

    private RequestRejectionSupport() {}

    static void setOrganisationId(HttpServletRequest request, Long organisationId) {
        request.setAttribute(ORGANISATION_ID_ATTRIBUTE, organisationId);
    }

    static String getOrganisationId(HttpServletRequest request) {
        Object value = request.getAttribute(ORGANISATION_ID_ATTRIBUTE);
        return value == null ? null : String.valueOf(value);
    }

    static String extractClientId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof EnhancedPrincipal principal)) {
            return null;
        }
        String clientId = principal.clientId();
        return (clientId == null || clientId.isEmpty()) ? null : clientId;
    }

    static void writeError(
            HttpServletResponse response, ObjectMapper objectMapper, int status, String message, String errorId)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse errorResponse = new ErrorResponse(status, message, errorId);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
