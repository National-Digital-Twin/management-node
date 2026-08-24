/*
 * SPDX-License-Identifier: Apache-2.0
 * © Crown Copyright 2026. This work has been developed by the National Digital Twin Programme and is legally
 * attributed to the Department for Business and Trade (UK) as the governing entity.
 */

package uk.gov.dbt.ndtp.ia.node.management.service.providers.policy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.gov.dbt.ndtp.ia.node.management.config.OpaProperties;

/**
 * Invokes the PDP (OPA) with a policy decision request and interprets the response.
 * Any failure to reach or parse a response from the PDP is treated as DENY, so a PDP
 * outage fails closed rather than silently disabling policy enforcement.
 */
@Component
@Slf4j
public class PolicyDecisionClient {

    private final RestClient opaRestClient;
    private final String decisionPath;

    public PolicyDecisionClient(RestClient opaRestClient, OpaProperties opaProperties) {
        this.opaRestClient = opaRestClient;
        this.decisionPath = opaProperties.decisionPath();
    }

    public PolicyDecision evaluate(PolicyInput input) {
        try {
            PolicyDecisionResponse response = opaRestClient
                    .post()
                    .uri(decisionPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new PolicyDecisionRequest(input))
                    .retrieve()
                    .body(PolicyDecisionResponse.class);

            return Boolean.TRUE.equals(response != null ? response.result() : null) ? PolicyDecision.ALLOW
                    : PolicyDecision.DENY;
        } catch (Exception e) {
            log.warn("PDP invocation failed for resource {} action {}: {}", input.resource(), input.action(), e.toString());
            return PolicyDecision.DENY;
        }
    }
}
