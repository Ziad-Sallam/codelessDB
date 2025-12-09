package backend.SQLOptimization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.SQLOptimization.dto.OptimizeSQLResponse;
import backend.SQLOptimization.exceptions.SQLOptimizationException.GeminiAPIException;
import backend.SQLOptimization.exceptions.SQLOptimizationException.InvalidSQLException;
import backend.SQLOptimization.service.SQLOptimizationService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SQLOptimizationServiceTest {

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private ObjectMapper objectMapper;

  @InjectMocks
  private SQLOptimizationService optimizationService;

  private final String testApiKey = "test-api-key";
  private final String testApiUrl = "https://test-api.com";

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(optimizationService, "geminiApiKey", testApiKey);
    ReflectionTestUtils.setField(optimizationService, "geminiApiUrl", testApiUrl);
  }

  @Test
  void testOptimizeSQL_Success() throws Exception {
    String inputSQL = "SELECT * FROM users";
    String geminiResponseBody = """
        {
          "candidates": [{
            "content": {
              "parts": [{
                "text": "{\\"optimizedSQL\\": \\"SELECT id, name FROM users\\", \\"summary\\": \\"Optimized by selecting specific columns\\"}"
              }]
            }
          }]
        }
        """;

    when(restTemplate.exchange(
        anyString(),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(String.class))).thenReturn(new ResponseEntity<>(geminiResponseBody, HttpStatus.OK));

    when(objectMapper.readTree(anyString()))
        .thenReturn(new ObjectMapper().readTree(geminiResponseBody))
        .thenReturn(new ObjectMapper().readTree(
            "{\"optimizedSQL\": \"SELECT id, name FROM users\", \"summary\": \"Optimized by selecting specific columns\"}"));

    when(objectMapper.writeValueAsString(anyString()))
        .thenReturn("\"prompt text\"");

    OptimizeSQLResponse response = optimizationService.optimizeSQL(inputSQL);

    assertNotNull(response);
    assertEquals("SELECT id, name FROM users", response.getOptimizedSQL());
    assertEquals("Optimized by selecting specific columns", response.getSummary());
    verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class));
  }

  @Test
  void testOptimizeSQL_EmptySQL_ThrowsInvalidSQLException() {
    assertThrows(InvalidSQLException.class, () -> {
      optimizationService.optimizeSQL("");
    });

    assertThrows(InvalidSQLException.class, () -> {
      optimizationService.optimizeSQL(null);
    });

    assertThrows(InvalidSQLException.class, () -> {
      optimizationService.optimizeSQL("   ");
    });

    verify(restTemplate, never()).exchange(anyString(), any(), any(), any(Class.class));
  }

  @Test
  void testOptimizeSQL_GeminiAPIFailure_ThrowsGeminiAPIException() throws Exception {
    String inputSQL = "SELECT * FROM users";

    when(objectMapper.writeValueAsString(anyString()))
        .thenReturn("\"prompt text\"");

    when(restTemplate.exchange(
        anyString(),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(String.class))).thenThrow(new RestClientException("API connection failed"));

    assertThrows(GeminiAPIException.class, () -> {
      optimizationService.optimizeSQL(inputSQL);
    });
  }

  @Test
  void testOptimizeSQL_InvalidResponseFormat_ThrowsGeminiAPIException() throws Exception {
    String inputSQL = "SELECT * FROM users";
    String invalidResponse = "{\"invalid\": \"format\"}";

    when(restTemplate.exchange(
        anyString(),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(String.class))).thenReturn(new ResponseEntity<>(invalidResponse, HttpStatus.OK));

    when(objectMapper.readTree(anyString()))
        .thenReturn(new ObjectMapper().readTree(invalidResponse));

    when(objectMapper.writeValueAsString(anyString()))
        .thenReturn("\"prompt text\"");

    assertThrows(GeminiAPIException.class, () -> {
      optimizationService.optimizeSQL(inputSQL);
    });
  }

  @Test
  void testOptimizeSQL_WithMarkdownCodeBlocks_Success() throws Exception {
    String inputSQL = "SELECT * FROM users";
    String geminiResponseBody = """
        {
          "candidates": [{
            "content": {
              "parts": [{
                "text": "```json\\n{\\"optimizedSQL\\": \\"SELECT id FROM users\\", \\"summary\\": \\"Optimized\\"\\n```"
              }]
            }
          }]
        }
        """;

    when(restTemplate.exchange(
        anyString(),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(String.class))).thenReturn(new ResponseEntity<>(geminiResponseBody, HttpStatus.OK));

    when(objectMapper.readTree(anyString()))
        .thenReturn(new ObjectMapper().readTree(geminiResponseBody))
        .thenReturn(
            new ObjectMapper().readTree("{\"optimizedSQL\": \"SELECT id FROM users\", \"summary\": \"Optimized\"}"));

    when(objectMapper.writeValueAsString(anyString()))
        .thenReturn("\"prompt text\"");

    OptimizeSQLResponse response = optimizationService.optimizeSQL(inputSQL);

    assertNotNull(response);
    assertEquals("SELECT id FROM users", response.getOptimizedSQL());
  }

  @Test
  void testOptimizeSQL_Non2xxResponse_ThrowsGeminiAPIException() throws Exception {
    String inputSQL = "SELECT * FROM users";

    when(restTemplate.exchange(
        anyString(),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(String.class))).thenReturn(new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR));

    when(objectMapper.writeValueAsString(anyString()))
        .thenReturn("\"prompt text\"");

    assertThrows(GeminiAPIException.class, () -> {
      optimizationService.optimizeSQL(inputSQL);
    });
  }
}
