package backend.SQLOptimization;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.SQLOptimization.controller.SQLOptimizationController;
import backend.SQLOptimization.dto.OptimizeSQLRequest;
import backend.SQLOptimization.dto.OptimizeSQLResponse;
import backend.SQLOptimization.exceptions.SQLOptimizationException.GeminiAPIException;
import backend.SQLOptimization.exceptions.SQLOptimizationException.InvalidSQLException;
import backend.SQLOptimization.exceptions.SQLOptimizationException.RateLimitExceededException;
import backend.SQLOptimization.exceptions.SQLOptimizationExceptionHandler;
import backend.SQLOptimization.service.SQLOptimizationService;
import backend.security.AuthUser;
import backend.security.GoogleSuccessHandler;
import backend.security.JwtExtractor;
import backend.security.JwtUtil;
import backend.user.UserService;
import backend.user.exceptions.UserException.QuotaExceededException;

/**
 * Unit tests for SQLOptimizationController
 */
@WebMvcTest({ SQLOptimizationController.class, SQLOptimizationExceptionHandler.class })
@AutoConfigureMockMvc(addFilters = false)
@Import(SQLOptimizationControllerTest.TestConfig.class)
class SQLOptimizationControllerTest {

    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                    java.security.Principal principal = webRequest.getUserPrincipal();
                    if (principal instanceof UsernamePasswordAuthenticationToken auth) {
                        return auth.getPrincipal();
                    }
                    return null;
                }
            });
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SQLOptimizationService optimizationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtExtractor jwtExtractor;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private GoogleSuccessHandler googleSuccessHandler;

    @Test
    void testOptimizeSQL_Success() throws Exception {
        // Given
        AuthUser authUser = new AuthUser(1, "testuser");
        OptimizeSQLRequest request = new OptimizeSQLRequest();
        request.setSqlCode("SELECT * FROM users");

        OptimizeSQLResponse response = new OptimizeSQLResponse(
            "SELECT id, name FROM users",
            "Optimized to select only necessary columns"
        );

        doNothing().when(userService).checkAndDecrementAiQuota(anyInt());
        when(optimizationService.optimizeSQL(any(String.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/optimize-sql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(new UsernamePasswordAuthenticationToken(authUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.optimizedSQL").value("SELECT id, name FROM users"))
                .andExpect(jsonPath("$.summary").value("Optimized to select only necessary columns"));

        verify(userService).checkAndDecrementAiQuota(anyInt());
        verify(optimizationService).optimizeSQL("SELECT * FROM users");
    }

    @Test
    void testOptimizeSQL_InvalidSQL_Returns400() throws Exception {
        // Given
        AuthUser authUser = new AuthUser(1, "testuser");
        OptimizeSQLRequest request = new OptimizeSQLRequest();
        request.setSqlCode("");

        doNothing().when(userService).checkAndDecrementAiQuota(anyInt());
        when(optimizationService.optimizeSQL(any(String.class)))
                .thenThrow(new InvalidSQLException("SQL code cannot be empty"));

        // When & Then
        mockMvc.perform(post("/api/optimize-sql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(new UsernamePasswordAuthenticationToken(authUser, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("SQL code cannot be empty"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testOptimizeSQL_QuotaExceeded_Returns429() throws Exception {
        // Given
        AuthUser authUser = new AuthUser(1, "testuser");
        OptimizeSQLRequest request = new OptimizeSQLRequest();
        request.setSqlCode("SELECT * FROM users");

        doThrow(new QuotaExceededException("AI quota exceeded"))
                .when(userService).checkAndDecrementAiQuota(anyInt());

        // When & Then
        mockMvc.perform(post("/api/optimize-sql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(new UsernamePasswordAuthenticationToken(authUser, null)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void testOptimizeSQL_GeminiAPIError_Returns503() throws Exception {
        // Given
        AuthUser authUser = new AuthUser(1, "testuser");
        OptimizeSQLRequest request = new OptimizeSQLRequest();
        request.setSqlCode("SELECT * FROM users");

        doNothing().when(userService).checkAndDecrementAiQuota(anyInt());
        when(optimizationService.optimizeSQL(any(String.class)))
                .thenThrow(new GeminiAPIException("Gemini API is currently unavailable"));

        // When & Then
        mockMvc.perform(post("/api/optimize-sql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(new UsernamePasswordAuthenticationToken(authUser, null)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Gemini API is currently unavailable"))
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void testOptimizeSQL_RateLimitExceeded_Returns429() throws Exception {
        // Given
        AuthUser authUser = new AuthUser(1, "testuser");
        OptimizeSQLRequest request = new OptimizeSQLRequest();
        request.setSqlCode("SELECT * FROM users");

        doNothing().when(userService).checkAndDecrementAiQuota(anyInt());
        when(optimizationService.optimizeSQL(any(String.class)))
                .thenThrow(new RateLimitExceededException("Rate limit exceeded for Gemini API"));

        // When & Then
        mockMvc.perform(post("/api/optimize-sql")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .principal(new UsernamePasswordAuthenticationToken(authUser, null)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Rate limit exceeded for Gemini API"))
                .andExpect(jsonPath("$.status").value(429));
    }
}
