package backend.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import backend.entities.User;
import backend.user.exceptions.UserException.*;

import java.sql.Date;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	private BCryptPasswordEncoder encoder;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		encoder = new BCryptPasswordEncoder();
	}

	// ------------------ CREATE USER ------------------

	@Test
	void testCreateUserSuccess() {
		UserDto dto = new UserDto();
		dto.setUsername("john");
		dto.setEmail("john@example.com");
		dto.setRawPassword("password123");

		when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
		when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);

		assertDoesNotThrow(() -> userService.createUser(dto));
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	void testCreateUserEmailExists() {
		UserDto dto = new UserDto();
		dto.setEmail("john@example.com");
		dto.setUsername("john");

		when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

		EmailAlreadyExistsException ex = assertThrows(EmailAlreadyExistsException.class,
				() -> userService.createUser(dto));
		assertEquals("Email already exists", ex.getMessage());
	}

	@Test
	void testCreateUserUsernameExists() {
		UserDto dto = new UserDto();
		dto.setEmail("john@example.com");
		dto.setUsername("john");

		when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
		when(userRepository.existsByUsername(dto.getUsername())).thenReturn(true);

		UsernameAlreadyExistsException ex = assertThrows(UsernameAlreadyExistsException.class,
				() -> userService.createUser(dto));
		assertEquals("Username already exists", ex.getMessage());
	}

	// ------------------ LOGIN ------------------

	@Test
	void testLoginSuccess() {
		User user = new User();
		user.setEmail("john@example.com");
		// encode once for stored password
		String encoded = new BCryptPasswordEncoder().encode("password123");
		user.setPassword(encoded);

		when(userRepository.findByEmail("john@example.com")).thenReturn(user);

		// test login with raw password
		assertDoesNotThrow(() -> userService.login("john@example.com", "password123"));
	}


	@Test
	void testLoginUserNotFound() {
		when(userRepository.findByEmail("unknown@example.com")).thenReturn(null);

		UserNotFoundException ex = assertThrows(UserNotFoundException.class,
				() -> userService.login("unknown@example.com", "password123"));
		assertEquals("User not found", ex.getMessage());
	}

	@Test
	void testLoginBadCredentials() {
		User user = new User();
		user.setEmail("john@example.com");
		user.setPassword(encoder.encode("password123"));

		when(userRepository.findByEmail("john@example.com")).thenReturn(user);

		assertThrows(BadCredentialsException.class,
				() -> userService.login("john@example.com", "wrongpass"));
	}

	// ------------------ GET USER INFO ------------------

	@Test
	void testGetUserInfoSuccess() {
		User user = new User();
		user.setUsername("john");
		user.setEmail("john@example.com");
		user.setPicture(new byte[] { 1, 2, 3 });
		user.setCreatedAt(new Date(System.currentTimeMillis()));

		when(userRepository.findById(1)).thenReturn(user);

		UserDto dto = userService.getUserInfo(1);
		assertEquals("john", dto.getUsername());
		assertEquals("john@example.com", dto.getEmail());
		assertArrayEquals(new byte[] { 1, 2, 3 }, dto.getPicture());
		assertNotNull(dto.getCreatedAt());
		assertNull(dto.getRawPassword());
	}

	@Test
	void testGetUserInfoNotFound() {
		when(userRepository.findById(1)).thenReturn(null);
		assertThrows(UserNotFoundException.class, () -> userService.getUserInfo(1));
	}

	// ------------------ UPDATE USER ------------------

	@Test
	void testUpdateUsername() {
		User user = new User();
		user.setUsername("john");
		user.setEmail("john@example.com");

		UserDto dto = new UserDto();
		dto.setUsername("newJohn");

		when(userRepository.findById(1)).thenReturn(user);
		when(userRepository.existsByUsername("newJohn")).thenReturn(false);

		assertDoesNotThrow(() -> userService.updateUser(dto, 1));
		assertEquals("newJohn", user.getUsername());
		verify(userRepository, times(1)).save(user);
	}

	@Test
	void testUpdateEmail() {
		User user = new User();
		user.setEmail("john@example.com");

		UserDto dto = new UserDto();
		dto.setEmail("new@example.com");

		when(userRepository.findById(1)).thenReturn(user);
		when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

		assertDoesNotThrow(() -> userService.updateUser(dto, 1));
		assertEquals("new@example.com", user.getEmail());
		verify(userRepository, times(1)).save(user);
	}

	@Test
	void testUpdatePassword() {
		User user = new User();
		user.setPassword(encoder.encode("oldpass"));

		UserDto dto = new UserDto();
		dto.setRawPassword("newpass");

		when(userRepository.findById(1)).thenReturn(user);

		assertDoesNotThrow(() -> userService.updateUser(dto, 1));
		assertTrue(encoder.matches("newpass", user.getPassword()));
		verify(userRepository, times(1)).save(user);
	}

	@Test
	void testUpdatePicture() {
		User user = new User();
		user.setPicture(new byte[] { 0 });

		UserDto dto = new UserDto();
		dto.setPicture(new byte[] { 1, 2, 3 });

		when(userRepository.findById(1)).thenReturn(user);

		assertDoesNotThrow(() -> userService.updateUser(dto, 1));
		assertArrayEquals(new byte[] { 1, 2, 3 }, user.getPicture());
		verify(userRepository, times(1)).save(user);
	}

	@Test
	void testUpdateUserNotFound() {
		UserDto dto = new UserDto();
		when(userRepository.findById(1)).thenReturn(null);

		assertThrows(UserNotFoundException.class, () -> userService.updateUser(dto, 1));
	}

	// ------------------ DELETE USER ------------------

	@Test
	void testDeleteUser() {
		assertDoesNotThrow(() -> userService.deleteUser(1));
		verify(userRepository, times(1)).deleteById(1);
	}
}
