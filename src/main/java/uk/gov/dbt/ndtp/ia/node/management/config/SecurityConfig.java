package uk.gov.dbt.ndtp.ia.node.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;
    private final ClientIdMdcFilter clientIdMdcFilter;
    
    public SecurityConfig(KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter,
                          ClientIdMdcFilter clientIdMdcFilter) {
        this.keycloakJwtAuthenticationConverter = keycloakJwtAuthenticationConverter;
        this.clientIdMdcFilter = clientIdMdcFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/actuator/**").permitAll()
                //.requestMatchers("/api/v1/configuration/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter))
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Add ClientIdMdcFilter after the BearerTokenAuthenticationFilter
            // This ensures the Authentication object is already set in the SecurityContext
            .addFilterAfter(clientIdMdcFilter, BearerTokenAuthenticationFilter.class);
        
        return http.build();
    }
}