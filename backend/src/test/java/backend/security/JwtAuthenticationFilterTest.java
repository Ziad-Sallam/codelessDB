package backend.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class JwtAuthenticationFilterTest {

	@Mock
	private JwtExtractor jwtExtractor;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private FilterChain filterChain;

	@InjectMocks
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		SecurityContextHolder.clearContext();
	}

	@Test
	void testShouldNotFilter() {
		// OPTIONS method
		when(request.getMethod()).thenReturn("OPTIONS");
		assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));

		// Login path
		when(request.getMethod()).thenReturn("POST");
		when(request.getRequestURI()).thenReturn("/user/login");
		assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));

		// Signup path
		when(request.getRequestURI()).thenReturn("/user/signup");
		assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));

		// OAuth2 paths
		when(request.getRequestURI()).thenReturn("/oauth2/authorization/google");
		assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));

		when(request.getRequestURI()).thenReturn("/login/oauth2/code/google");
		assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));

		// Protected path should be filtered
		when(request.getRequestURI()).thenReturn("/diagrams/get");
		assertFalse(jwtAuthenticationFilter.shouldNotFilter(request));
	}

	@Test
	void testDoFilterInternal_ValidToken() throws ServletException, IOException {
		String authHeader = "Bearer valid-token";
		AuthUser authUser = new AuthUser(1, "testuser");

		when(request.getHeader("Authorization")).thenReturn(authHeader);
		when(jwtExtractor.authenticate(authHeader, true)).thenReturn(authUser);

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertNotNull(SecurityContextHolder.getContext().getAuthentication());
		assertEquals(authUser, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void testDoFilterInternal_NoToken() throws ServletException, IOException {
		when(request.getHeader("Authorization")).thenReturn(null);
		when(jwtExtractor.authenticate(null, true)).thenReturn(null);

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(filterChain).doFilter(request, response);
	}

	@Test
	void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
		String authHeader = "Bearer invalid-token";
		String errorMessage = "Token expired";

		when(request.getHeader("Authorization")).thenReturn(authHeader);
		when(jwtExtractor.authenticate(authHeader, true)).thenThrow(
				new JwtValidationException(errorMessage, List.of(new OAuth2Error("invalid_token", errorMessage, null))));

		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		when(response.getWriter()).thenReturn(printWriter);

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		verify(filterChain, never()).doFilter(request, response);

		String responseContent = stringWriter.toString();
		assertTrue(responseContent.contains(errorMessage));
	}

	@Test
	void testDoFilterInternal_AlreadyAuthenticated() throws ServletException, IOException {
		SecurityContextHolder.getContext()
				.setAuthentication(mock(org.springframework.security.core.Authentication.class));

		jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

		verify(jwtExtractor, never()).authenticate(anyString(), anyBoolean());
		verify(filterChain).doFilter(request, response);
	}
}
