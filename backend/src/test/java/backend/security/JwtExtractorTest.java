package backend.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

class JwtExtractorTest {

	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private JwtExtractor jwtExtractor;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testAuthenticate_NullToken() {
		assertNull(jwtExtractor.authenticate(null, true));
		assertNull(jwtExtractor.authenticate(null, false));
	}

	@Test
	void testAuthenticate_NoBearerInHeader() {
		assertNull(jwtExtractor.authenticate("InvalidHeader", true));
	}

	@Test
	void testAuthenticate_Success_InHeader() {
		String token = "valid-token";
		String authHeader = "Bearer " + token;
		int userId = 1;
		String username = "testuser";

		when(jwtUtil.extractSubject(token)).thenReturn(userId);
		when(jwtUtil.extractUsername(token)).thenReturn(username);
		when(jwtUtil.isTokenValid(token, userId)).thenReturn(true);

		AuthUser result = jwtExtractor.authenticate(authHeader, true);

		assertNotNull(result);
		assertEquals(userId, result.userId());
		assertEquals(username, result.username());
	}

	@Test
	void testAuthenticate_Success_NotInHeader() {
		String token = "valid-token";
		int userId = 1;
		String username = "testuser";

		when(jwtUtil.extractSubject(token)).thenReturn(userId);
		when(jwtUtil.extractUsername(token)).thenReturn(username);
		when(jwtUtil.isTokenValid(token, userId)).thenReturn(true);

		AuthUser result = jwtExtractor.authenticate(token, false);

		assertNotNull(result);
		assertEquals(userId, result.userId());
		assertEquals(username, result.username());
	}

	@Test
	void testAuthenticate_ValidationFailed() {
		String token = "invalid-token";
		int userId = 1;

		when(jwtUtil.extractSubject(token)).thenReturn(userId);
		when(jwtUtil.isTokenValid(token, userId)).thenReturn(false);

		assertThrows(JwtValidationException.class, () -> jwtExtractor.authenticate(token, false));
	}

	@Test
	void testAuthenticate_ExpiredToken() {
		String token = "expired-token";
		when(jwtUtil.extractSubject(token)).thenThrow(mock(ExpiredJwtException.class));

		JwtValidationException ex = assertThrows(JwtValidationException.class,
				() -> jwtExtractor.authenticate(token, false));
		assertEquals("Token expired", ex.getMessage());
	}

	@Test
	void testAuthenticate_InvalidSignature() {
		String token = "bad-sig-token";
		when(jwtUtil.extractSubject(token)).thenThrow(mock(SignatureException.class));

		JwtValidationException ex = assertThrows(JwtValidationException.class,
				() -> jwtExtractor.authenticate(token, false));
		assertEquals("Invalid token signature", ex.getMessage());
	}

	@Test
	void testAuthenticate_MalformedToken() {
		String token = "malformed-token";
		when(jwtUtil.extractSubject(token)).thenThrow(mock(MalformedJwtException.class));

		JwtValidationException ex = assertThrows(JwtValidationException.class,
				() -> jwtExtractor.authenticate(token, false));
		assertEquals("Malformed JWT token", ex.getMessage());
	}

	@Test
	void testAuthenticate_IllegalArgument() {
		String token = "empty-token";
		when(jwtUtil.extractSubject(token)).thenThrow(new IllegalArgumentException("error"));

		JwtValidationException ex = assertThrows(JwtValidationException.class,
				() -> jwtExtractor.authenticate(token, false));
		assertEquals("Invalid or empty JWT token", ex.getMessage());
	}

	@Test
	void testAuthenticate_GeneralException() {
		String token = "error-token";
		when(jwtUtil.extractSubject(token)).thenThrow(new RuntimeException("general error"));

		JwtValidationException ex = assertThrows(JwtValidationException.class,
				() -> jwtExtractor.authenticate(token, false));
		assertEquals("Invalid token", ex.getMessage());
	}
}
