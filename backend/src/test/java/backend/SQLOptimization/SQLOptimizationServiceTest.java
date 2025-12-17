package backend.SQLOptimization;

import backend.SQLOptimization.dto.OptimizeSQLResponse;
import backend.SQLOptimization.exceptions.SQLOptimizationException.GeminiAPIException;
import backend.SQLOptimization.exceptions.SQLOptimizationException.InvalidSQLException;
import backend.SQLOptimization.service.SQLOptimizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Full working unit tests for SQLOptimizationService.
 */
class SQLOptimizationServiceTest {

    private RestTemplate restTemplate;
    private SQLOptimizationService service;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper();
        service = new SQLOptimizationService(restTemplate, objectMapper);

        ReflectionTestUtils.setField(service, "geminiApiKey", "testKey");
        ReflectionTestUtils.setField(service, "geminiApiUrl", "http://test-gemini.test/api");

        // Mute SQLOptimizationService logger to avoid printing errors during tests
        Logger logger = (Logger) LoggerFactory.getLogger(SQLOptimizationService.class);
        logger.setLevel(Level.OFF);
    }

    @Test
    void testOptimizeSQL_EmptySQL_ThrowsInvalidSQLException() {
        assertThrows(InvalidSQLException.class, () -> service.optimizeSQL(""));
        assertThrows(InvalidSQLException.class, () -> service.optimizeSQL("   "));
        assertThrows(InvalidSQLException.class, () -> service.optimizeSQL(null));
    }

    @Test
    void testOptimizeSQL_SuccessfulResponse_ReturnsOptimizedSQL() {
        String geminiRawResponse = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "{\\"optimizedSQL\\":\\"SELECT id, name FROM users\\",\\"summary\\":\\"Selected only necessary columns\\"}"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        ResponseEntity<String> entity = new ResponseEntity<>(geminiRawResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(entity);

        OptimizeSQLResponse resp = service.optimizeSQL("SELECT * FROM users");

        assertNotNull(resp);
        assertEquals("SELECT id, name FROM users", resp.getOptimizedSQL());
        assertEquals("Selected only necessary columns", resp.getSummary());
    }

    @Test
    void testOptimizeSQL_WithMarkdownCodeBlock_Success() {
        String geminiRawResponse = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "```json\\n{\\"optimizedSQL\\":\\"SELECT id FROM users\\",\\"summary\\":\\"Optimized\\"}\\n```"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        ResponseEntity<String> entity = new ResponseEntity<>(geminiRawResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(entity);

        OptimizeSQLResponse resp = service.optimizeSQL("SELECT * FROM users");

        assertNotNull(resp);
        assertEquals("SELECT id FROM users", resp.getOptimizedSQL());
        assertEquals("Optimized", resp.getSummary());
    }

    @Test
    void testOptimizeSQL_Non2xxResponse_ThrowsGeminiAPIException() {
        ResponseEntity<String> entity = new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenReturn(entity);

        GeminiAPIException ex = assertThrows(GeminiAPIException.class, () -> service.optimizeSQL("SELECT * FROM users"));
        assertTrue(ex.getMessage().contains("Gemini API returned error"));
    }

    @Test
    void testOptimizeSQL_RestClientException_ThrowsGeminiAPIException() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
                .thenThrow(new RestClientException("API down"));

        GeminiAPIException ex = assertThrows(GeminiAPIException.class, () -> service.optimizeSQL("SELECT * FROM users"));
        assertTrue(ex.getMessage().contains("Failed to call Gemini API"));
    }
}
