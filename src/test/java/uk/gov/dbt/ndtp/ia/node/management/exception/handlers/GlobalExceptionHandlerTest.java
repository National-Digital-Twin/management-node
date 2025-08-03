package uk.gov.dbt.ndtp.ia.node.management.exception.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import uk.gov.dbt.ndtp.ia.node.management.exception.AuthenticationProcessingException;
import uk.gov.dbt.ndtp.ia.node.management.exception.ErrorResponse;
import uk.gov.dbt.ndtp.ia.node.management.exception.JwtClaimParsingException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the GlobalExceptionHandler class.
 * Verifies that each exception handler method returns the correct HTTP status code
 * and ErrorResponse object with appropriate values.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleAuthenticationProcessingException_shouldReturnUnauthorizedStatus() {
        // Arrange
        String clientId = "test-client";
        String message = "Authentication failed";
        AuthenticationProcessingException exception = new AuthenticationProcessingException(message, clientId);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationProcessingException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains(message));
        assertNotNull(errorResponse.getErrorId());
    }

    @Test
    void handleAuthenticationProcessingException_withSubclass_shouldReturnUnauthorizedStatus() {
        // Arrange
        String clientId = "test-client";
        String message = "JWT claim parsing failed";
        JwtClaimParsingException exception = new JwtClaimParsingException(message, clientId);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationProcessingException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains(message));
        assertNotNull(errorResponse.getErrorId());
    }

    @Test
    void handleRuntimeException_shouldReturnInternalServerErrorStatus() {
        // Arrange
        String message = "Something went wrong";
        RuntimeException exception = new RuntimeException(message);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRuntimeException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals("An internal server error occurred", errorResponse.getMessage());
        assertNotNull(errorResponse.getErrorId());
    }

    @Test
    void handleAllExceptions_shouldReturnInternalServerErrorStatus() {
        // Arrange
        String message = "Generic exception";
        Exception exception = new Exception(message);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAllExceptions(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals("An unexpected error occurred", errorResponse.getMessage());
        assertNotNull(errorResponse.getErrorId());
    }
}