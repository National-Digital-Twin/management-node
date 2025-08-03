package uk.gov.dbt.ndtp.ia.node.management.exception;

/**
 * Exception thrown when there's an error parsing the resource access information
 * from the authentication token or introspection data.
 */
public class ResourceAccessParsingException extends AuthenticationProcessingException {
    
    /**
     * Constructs a new ResourceAccessParsingException with the specified detail message.
     *
     * @param message the detail message
     * @param clientId the client ID associated with the authentication
     */
    public ResourceAccessParsingException(String message, String clientId) {
        super(message, clientId);
    }
    
    /**
     * Constructs a new ResourceAccessParsingException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     * @param clientId the client ID associated with the authentication
     */
    public ResourceAccessParsingException(String message, Throwable cause, String clientId) {
        super(message, cause, clientId);
    }
}