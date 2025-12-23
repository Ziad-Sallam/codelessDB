package backend.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthUserTest {

	@Test
	void testAuthUserRecord() {
		AuthUser authUser = new AuthUser(1, "testuser");

		assertEquals(1, authUser.userId());
		assertEquals("testuser", authUser.username());
	}

	@Test
	void testEqualsAndHashCode() {
		AuthUser authUser1 = new AuthUser(1, "testuser");
		AuthUser authUser2 = new AuthUser(1, "testuser");
		AuthUser authUser3 = new AuthUser(2, "otheruser");

		assertEquals(authUser1, authUser2);
		assertNotEquals(authUser1, authUser3);
		assertEquals(authUser1.hashCode(), authUser2.hashCode());
		assertNotEquals(authUser1.hashCode(), authUser3.hashCode());
	}

	@Test
	void testToString() {
		AuthUser authUser = new AuthUser(1, "testuser");
		String toString = authUser.toString();

		assertTrue(toString.contains("userId=1"));
		assertTrue(toString.contains("username=testuser"));
	}
}
