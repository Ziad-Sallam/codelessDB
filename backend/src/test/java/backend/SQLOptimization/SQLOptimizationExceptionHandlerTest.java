package backend.SQLOptimization;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import backend.SQLOptimization.exceptions.SQLOptimizationException.GeminiAPIException;
import backend.SQLOptimization.exceptions.SQLOptimizationException.InvalidSQLException;
import backend.SQLOptimization.exceptions.SQLOptimizationException.RateLimitExceededException;
import backend.SQLOptimization.exceptions.SQLOptimizationExceptionHandler;
import backend.config.ErrorResponse;

/**
 * Unit tests for SQLOptimizationExceptionHandler
 */
class SQLOptimizationExceptionHandlerTest {

    private SQLOptimizationExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new SQLOptimizationExceptionHandler();
    }

    @Test
    void testHandleInvalidSQL_Returns400() {
        // Given
        InvalidSQLException exception = new InvalidSQLException("SQL code cannot be empty");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidSQL(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SQL code cannot be empty", response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void testHandleGeminiAPI_Returns503() {
        // Given
        GeminiAPIException exception = new GeminiAPIException("Gemini API is currently unavailable");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeminiAPI(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Gemini API is currently unavailable", response.getBody().getMessage());
        assertEquals(503, response.getBody().getStatus());
    }

    @Test
    void testHandleRateLimit_Returns429() {
        // Given
        RateLimitExceededException exception = new RateLimitExceededException("Rate limit exceeded for Gemini API");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRateLimit(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Rate limit exceeded for Gemini API", response.getBody().getMessage());
        assertEquals(429, response.getBody().getStatus());
    }

    @Test
    void testHandleInvalidSQL_WithDifferentMessage() {
        // Given
        InvalidSQLException exception = new InvalidSQLException("Invalid SQL syntax");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidSQL(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid SQL syntax", response.getBody().getMessage());
    }

    @Test
    void testHandleGeminiAPI_WithDifferentMessage() {
        // Given
        GeminiAPIException exception = new GeminiAPIException("Failed to call Gemini API: Network error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeminiAPI(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Failed to call Gemini API: Network error", response.getBody().getMessage());
    }

    @Test
    void testHandleRateLimit_WithDifferentMessage() {
        // Given
        RateLimitExceededException exception = new RateLimitExceededException("Too many requests, please try again later");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRateLimit(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("Too many requests, please try again later", response.getBody().getMessage());
    }
}
