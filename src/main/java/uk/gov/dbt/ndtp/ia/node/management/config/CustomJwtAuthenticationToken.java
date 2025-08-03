package uk.gov.dbt.ndtp.ia.node.management.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import uk.gov.dbt.ndtp.ia.node.management.model.jwt.EnhancedPrincipal;

import java.util.Collection;

/**
 * Custom JWT Authentication Token that uses CustomPrincipal as the principal object.
 * This allows access to the clientId in addition to the standard JWT information.
 */
public class CustomJwtAuthenticationToken extends JwtAuthenticationToken {
    
    private final EnhancedPrincipal principal;
    
    /**
     * Constructs a CustomJwtAuthenticationToken with the provided JWT, authorities, and CustomPrincipal.
     *
     * @param jwt the JWT
     * @param authorities the collection of granted authorities
     * @param principal the custom principal containing subject and clientId
     */
    public CustomJwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, EnhancedPrincipal principal) {
        super(jwt, authorities, principal.getSubject());
        this.principal = principal;
    }
    
    @Override
    public EnhancedPrincipal getPrincipal() {
        return this.principal;
    }
}