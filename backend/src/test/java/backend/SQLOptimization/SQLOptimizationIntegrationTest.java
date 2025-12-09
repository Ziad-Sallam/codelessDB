package backend.SQLOptimization;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.SQLOptimization.dto.OptimizeSQLRequest;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SQLOptimizationIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private RestTemplate restTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @WithMockUser
  void testEndToEndOptimization_Success() throws Exception {
    String geminiResponse = """
        {
          "candidates": [{
            "content": {
              "parts": [{
                "text": "{\\"optimizedSQL\\": \\"SELECT id, name FROM users WHERE active = 1\\", \\"summary\\": \\"Added index and optimized WHERE clause\\"}"
              }]
            }
          }]
        }
        """;

    when(restTemplate.exchange(
        anyString(),
        eq(HttpMethod.POST),
        any(HttpEntity.class),
        eq(String.class))).thenReturn(new ResponseEntity<>(geminiResponse, HttpStatus.OK));

    OptimizeSQLRequest request = new OptimizeSQLRequest();
    request.setSqlCode("SELECT * FROM users WHERE active = 1");

    mockMvc.perform(post("/api/optimize-sql")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.optimizedSQL").exists())
        .andExpect(jsonPath("$.summary").exists());
  }
}
