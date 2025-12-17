package backend.SQLOptimization;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.SQLOptimization.dto.OptimizeSQLRequest;
import backend.security.AuthUser;
import backend.security.GoogleSuccessHandler;
import backend.security.JwtAuthenticationFilter;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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

    @MockBean
    private backend.user.UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    JwtAuthenticationFilter jwtFilter;

    @MockBean
    GoogleSuccessHandler googleSuccessHandler;

    @Test
    void testEndToEndOptimization_Success() throws Exception {
        // Mock Gemini response
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

        // Mock RestTemplate exchange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(new ResponseEntity<>(geminiResponse, HttpStatus.OK));

        // Create a mock AuthUser
        AuthUser authUser = new AuthUser(1, "testUser");
        Authentication auth = new UsernamePasswordAuthenticationToken(authUser, null, Collections.emptyList());

        // Mock UserService to do nothing for quota check
        doNothing().when(userService).checkAndDecrementAiQuota(authUser.userId());

        // Prepare request
        OptimizeSQLRequest request = new OptimizeSQLRequest();
        request.setSqlCode("SELECT * FROM users WHERE active = 1");

        // Perform POST request
        mockMvc.perform(post("/api/optimize-sql")
                .with(csrf())
                .with(authentication(auth))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.optimizedSQL")
                        .value("SELECT id, name FROM users WHERE active = 1"))
                .andExpect(jsonPath("$.summary")
                        .value("Added index and optimized WHERE clause"));
    }
}
