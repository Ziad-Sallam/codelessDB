package backend.security;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import backend.entities.User;
import backend.user.UserDto;
import backend.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class GoogleSuccessHandlerTest {

	@Mock
	private JwtUtil jwtUtil;

	@Mock
	private UserService userService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private Authentication authentication;

	@Mock
	private OAuth2User oAuth2User;

	@InjectMocks
	private GoogleSuccessHandler googleSuccessHandler;

	private final String frontUrl = "http://localhost:3000";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(googleSuccessHandler, "frontUrl", frontUrl);
	}

	@Test
	void testOnAuthenticationSuccess_ExistingUser() throws IOException {
		String email = "test@example.com";
		String name = "Test User";
		String picture = "pic.png";
		User user = new User();
		user.setId(1);
		user.setUsername("test_user");

		when(authentication.getPrincipal()).thenReturn(oAuth2User);
		when(oAuth2User.getAttribute("email")).thenReturn(email);
		when(oAuth2User.getAttribute("name")).thenReturn(name);
		when(oAuth2User.getAttribute("picture")).thenReturn(picture);
		when(userService.findUserByEmail(email)).thenReturn(user);
		when(jwtUtil.generateToken(user.getId(), user.getUsername())).thenReturn("jwt-token");

		googleSuccessHandler.onAuthenticationSuccess(request, response, authentication);

		verify(response).sendRedirect(frontUrl + "/login?token=jwt-token");
	}

	@Test
	void testOnAuthenticationSuccess_NewUser() throws IOException {
		String email = "new@example.com";
		String name = "New User";
		String picture = "pic.png";
		User user = new User();
		user.setId(2);
		user.setUsername("New_User");

		when(authentication.getPrincipal()).thenReturn(oAuth2User);
		when(oAuth2User.getAttribute("email")).thenReturn(email);
		when(oAuth2User.getAttribute("name")).thenReturn(name);
		when(oAuth2User.getAttribute("picture")).thenReturn(picture);

		// First call returns null, second call (after creation) returns user
		when(userService.findUserByEmail(email)).thenReturn(null, user);
		when(userService.findUserByUsername(anyString())).thenReturn(null);
		when(jwtUtil.generateToken(user.getId(), user.getUsername())).thenReturn("jwt-token");

		googleSuccessHandler.onAuthenticationSuccess(request, response, authentication);

		verify(userService).createUser(any(UserDto.class));
		verify(response).sendRedirect(frontUrl + "/login?token=jwt-token");
	}

	@Test
	void testOnAuthenticationSuccess_UsernameConflict() throws IOException {
		String email = "conflict@example.com";
		String name = "Conflict User";
		User user = new User();
		user.setId(3);
		user.setUsername("Conflict_User100");

		when(authentication.getPrincipal()).thenReturn(oAuth2User);
		when(oAuth2User.getAttribute("email")).thenReturn(email);
		when(oAuth2User.getAttribute("name")).thenReturn(name);

		when(userService.findUserByEmail(email)).thenReturn(null, user);
		// Simulate username conflict once
		when(userService.findUserByUsername("Conflict_User")).thenReturn(new User());
		when(userService.findUserByUsername(contains("ConflictUser"))).thenReturn(null);

		when(jwtUtil.generateToken(user.getId(), user.getUsername())).thenReturn("jwt-token");

		googleSuccessHandler.onAuthenticationSuccess(request, response, authentication);

		verify(userService, atLeast(1)).findUserByUsername(anyString());
		verify(userService).createUser(any(UserDto.class));
		verify(response).sendRedirect(frontUrl + "/login?token=jwt-token");
	}

	@Test
	void testOnAuthenticationSuccess_Exception() throws IOException {
		when(authentication.getPrincipal()).thenThrow(new RuntimeException("Error"));

		googleSuccessHandler.onAuthenticationSuccess(request, response, authentication);

		verify(response).sendRedirect(frontUrl + "/login?error=oauth_failed");
	}
}
