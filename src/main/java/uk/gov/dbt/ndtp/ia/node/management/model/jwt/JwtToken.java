package uk.gov.dbt.ndtp.ia.node.management.model.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents the structure of a JWT token.
 * This class is used to deserialize the JSON response from the token introspection endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtToken {
    private Long exp;
    private Long iat;
    private String jti;
    private String iss;
    private List<String> aud;
    private String sub;
    private String typ;
    private String azp;
    private List<String> allowedOrigins;
    private Map<String, ResourceAccess> resourceAccess;
    private String scope;
    private String clientId;
    private String username;
    private String tokenType;
    private Boolean active;

    /**
     * Represents the resource access structure in the JWT token.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceAccess {
        private List<String> roles;
    }
}