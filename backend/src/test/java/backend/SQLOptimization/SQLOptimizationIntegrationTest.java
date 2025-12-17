package backend.SQLOptimization;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.SQLOptimization.dto.OptimizeSQLRequest;
import backend.SQLOptimization.dto.OptimizeSQLResponse;
import backend.SQLOptimization.service.SQLOptimizationService;
import backend.security.AuthUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // disable security filters for simplicity
class SQLOptimizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private backend.user.UserService userService;

    @MockBean
    private SQLOptimizationService sqlOptimizationService;

    @Test
    void testEndToEndOptimization_Success() throws Exception {

        // Mock AuthUser
        AuthUser authUser = new AuthUser(1, "testUser");
        Authentication auth = new UsernamePasswordAuthenticationToken(authUser, null, Collections.emptyList());

        // Mock SQLOptimizationService
        OptimizeSQLResponse mockResponse = new OptimizeSQLResponse(
                "SELECT id, name FROM users WHERE active = 1",
                "Added index and optimized WHERE clause"
        );
        when(sqlOptimizationService.optimizeSQL(anyString()))
                .thenReturn(mockResponse);

        // Mock UserService quota check
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
