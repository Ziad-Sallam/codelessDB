package backend.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

	private JwtUtil jwtUtil;
	private final long expirationMs = 3600000; // 1 hour

	@BeforeEach
	void setUp() {
		jwtUtil = new JwtUtil();
		ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", expirationMs);
	}

	@Test
	void testGenerateAndExtract() {
		int userId = 123;
		String username = "testuser";

		String token = jwtUtil.generateToken(userId, username);
		assertNotNull(token);

		assertEquals(userId, jwtUtil.extractUserId(token));
		assertEquals(username, jwtUtil.extractUsername(token));
		assertEquals(userId, jwtUtil.extractSubject(token));
		assertNotNull(jwtUtil.extractExpiration(token));
	}

	@Test
	void testIsTokenValid() {
		int userId = 123;
		String username = "testuser";
		String token = jwtUtil.generateToken(userId, username);

		assertTrue(jwtUtil.isTokenValid(token, userId));
		assertFalse(jwtUtil.isTokenValid(token, 456));
		assertTrue(jwtUtil.isTokenValid(token));
	}

	@Test
	void testIsTokenExpired() {
		int userId = 123;
		String username = "testuser";

		// Set short expiration for testing
		ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", -1000L); // Expired 1 second ago
		String token = jwtUtil.generateToken(userId, username);

		assertTrue(jwtUtil.isTokenExpired(token));
		assertFalse(jwtUtil.isTokenValid(token, userId));
	}

	@Test
	void testIsTokenValid_InvalidToken() {
		assertFalse(jwtUtil.isTokenValid("invalid.token.here"));
	}

	@Test
	void testRefreshToken() {
		int userId = 123;
		String username = "testuser";
		String token = jwtUtil.generateToken(userId, username);

		String newToken = jwtUtil.refreshToken(token);
		assertNotNull(newToken);
		assertEquals(userId, jwtUtil.extractUserId(newToken));
		assertEquals(username, jwtUtil.extractUsername(newToken));
	}
}
