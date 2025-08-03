package uk.gov.dbt.ndtp.ia.node.management.model.jwt;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Custom Principal object that includes clientId information from the JWT.
 */
@Getter
public class EnhancedPrincipal implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * -- GETTER --
     *  Get the subject (user identifier)
     *
     */
    private final String subject;
    /**
     * -- GETTER --
     *  Get the client ID
     *
     */
    private final String clientId;
    
    public EnhancedPrincipal(String subject, String clientId) {
        this.subject = subject;
        this.clientId = clientId;
    }


    @Override
    public String toString() {
        return "CustomPrincipal{" +
                "subject='" + subject + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}