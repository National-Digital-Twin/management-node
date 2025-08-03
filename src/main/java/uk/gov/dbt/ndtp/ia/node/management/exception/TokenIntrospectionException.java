package uk.gov.dbt.ndtp.ia.node.management.exception;

/**
 * Exception thrown when there's an error during token introspection.
 * This can happen when the introspection endpoint is unavailable, returns an error,
 * or when the introspection data is invalid.
 */
public class TokenIntrospectionException extends AuthenticationProcessingException {
    
    /**
     * Constructs a new TokenIntrospectionException with the specified detail message.
     *
     * @param message the detail message
     * @param clientId the client ID associated with the authentication
     */
    public TokenIntrospectionException(String message, String clientId) {
        super(message, clientId);
    }
    
    /**
     * Constructs a new TokenIntrospectionException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     * @param clientId the client ID associated with the authentication
     */
    public TokenIntrospectionException(String message, Throwable cause, String clientId) {
        super(message, cause, clientId);
    }
}