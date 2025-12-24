package backend.SQLOptimization;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import backend.SQLOptimization.exceptions.SQLOptimizationException.GeminiAPIException;
import backend.SQLOptimization.exceptions.SQLOptimizationException.InvalidSQLException;
import backend.SQLOptimization.exceptions.SQLOptimizationException.RateLimitExceededException;

/**
 * Unit tests for SQLOptimizationException inner classes
 */
class SQLOptimizationExceptionTest {

    @Test
    void testGeminiAPIException_Creation() {
        // Given
        String message = "Gemini API is currently unavailable";

        // When
        GeminiAPIException exception = new GeminiAPIException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testGeminiAPIException_CanBeThrown() {
        // Given
        String message = "Failed to call Gemini API";

        // When & Then
        GeminiAPIException exception = assertThrows(GeminiAPIException.class, () -> {
            throw new GeminiAPIException(message);
        });

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testInvalidSQLException_Creation() {
        // Given
        String message = "SQL code cannot be empty";

        // When
        InvalidSQLException exception = new InvalidSQLException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testInvalidSQLException_CanBeThrown() {
        // Given
        String message = "Invalid SQL syntax";

        // When & Then
        InvalidSQLException exception = assertThrows(InvalidSQLException.class, () -> {
            throw new InvalidSQLException(message);
        });

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testRateLimitExceededException_Creation() {
        // Given
        String message = "Rate limit exceeded for Gemini API";

        // When
        RateLimitExceededException exception = new RateLimitExceededException(message);

        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testRateLimitExceededException_CanBeThrown() {
        // Given
        String message = "Too many requests";

        // When & Then
        RateLimitExceededException exception = assertThrows(RateLimitExceededException.class, () -> {
            throw new RateLimitExceededException(message);
        });

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testAllExceptions_AreRuntimeExceptions() {
        // Given & When
        GeminiAPIException geminiEx = new GeminiAPIException("test");
        InvalidSQLException invalidEx = new InvalidSQLException("test");
        RateLimitExceededException rateLimitEx = new RateLimitExceededException("test");

        // Then - all should be instances of RuntimeException
        assertNotNull(geminiEx);
        assertNotNull(invalidEx);
        assertNotNull(rateLimitEx);
        
        // Verify they extend RuntimeException
        assertEquals(RuntimeException.class, geminiEx.getClass().getSuperclass());
        assertEquals(RuntimeException.class, invalidEx.getClass().getSuperclass());
        assertEquals(RuntimeException.class, rateLimitEx.getClass().getSuperclass());
    }

    @Test
    void testExceptions_WithNullMessage() {
        // When
        GeminiAPIException geminiEx = new GeminiAPIException(null);
        InvalidSQLException invalidEx = new InvalidSQLException(null);
        RateLimitExceededException rateLimitEx = new RateLimitExceededException(null);

        // Then - should not throw NPE, just have null message
        assertEquals(null, geminiEx.getMessage());
        assertEquals(null, invalidEx.getMessage());
        assertEquals(null, rateLimitEx.getMessage());
    }

    @Test
    void testExceptions_WithEmptyMessage() {
        // Given
        String emptyMessage = "";

        // When
        GeminiAPIException geminiEx = new GeminiAPIException(emptyMessage);
        InvalidSQLException invalidEx = new InvalidSQLException(emptyMessage);
        RateLimitExceededException rateLimitEx = new RateLimitExceededException(emptyMessage);

        // Then
        assertEquals(emptyMessage, geminiEx.getMessage());
        assertEquals(emptyMessage, invalidEx.getMessage());
        assertEquals(emptyMessage, rateLimitEx.getMessage());
    }
}
