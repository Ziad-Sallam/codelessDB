package backend.user;

import backend.entities.User;
import backend.user.exceptions.UserException.EmailAlreadyExistsException;
import backend.user.exceptions.UserException.InvalidEmailException;
import backend.user.exceptions.UserException.UserNotFoundException;
import backend.user.exceptions.UserException.UsernameAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void createUser_success() {
		UserDto dto = new UserDto();
		dto.setEmail("test@mail.com");
		dto.setUsername("john");
		dto.setRawPassword("123");

		when(userRepository.existsByEmail("test@mail.com")).thenReturn(false);
		when(userRepository.existsByUsername("john")).thenReturn(false);

		userService.createUser(dto);
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	void createUser_missingEmail() {
		UserDto dto = new UserDto();
		dto.setUsername("john");
		dto.setRawPassword("123");

		assertThrows(IllegalArgumentException.class, () -> userService.createUser(dto));
	}

	@Test
	void createUser_missingUsername() {
		UserDto dto = new UserDto();
		dto.setEmail("test@mail.com");
		dto.setRawPassword("123");

		assertThrows(IllegalArgumentException.class, () -> userService.createUser(dto));
	}

	@Test
	void createUser_missingPassword() {
		UserDto dto = new UserDto();
		dto.setEmail("test@mail.com");
		dto.setUsername("john");

		assertThrows(IllegalArgumentException.class, () -> userService.createUser(dto));
	}

	@Test
	void createUser_invalidEmail() {
		UserDto dto = new UserDto();
		dto.setEmail("bad-email");
		dto.setUsername("john");
		dto.setRawPassword("123");

		assertThrows(InvalidEmailException.class, () -> userService.createUser(dto));
	}

	@Test
	void createUser_emailAlreadyExists() {
		UserDto dto = new UserDto();
		dto.setEmail("test@mail.com");
		dto.setUsername("john");
		dto.setRawPassword("123");

		when(userRepository.existsByEmail("test@mail.com")).thenReturn(true);

		assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(dto));
	}

	@Test
	void createUser_usernameAlreadyExists() {
		UserDto dto = new UserDto();
		dto.setEmail("test@mail.com");
		dto.setUsername("john");
		dto.setRawPassword("123");

		when(userRepository.existsByEmail("test@mail.com")).thenReturn(false);
		when(userRepository.existsByUsername("john")).thenReturn(true);

		assertThrows(UsernameAlreadyExistsException.class, () -> userService.createUser(dto));
	}

	@Test
	void login_success() {
		User user = new User();
		user.setEmail("test@mail.com");
		user.setPassword(new BCryptPasswordEncoder().encode("123"));

		when(userRepository.findByEmail("test@mail.com")).thenReturn(user);

		assertDoesNotThrow(() -> userService.login("test@mail.com", "123"));
	}

	@Test
	void login_userNotFound() {
		when(userRepository.findByEmail("test@mail.com")).thenReturn(null);

		assertThrows(UserNotFoundException.class, () -> userService.login("test@mail.com", "123"));
	}

	@Test
	void login_invalidPassword() {
		User user = new User();
		user.setEmail("test@mail.com");
		user.setPassword(new BCryptPasswordEncoder().encode("correct"));

		when(userRepository.findByEmail("test@mail.com")).thenReturn(user);

		assertThrows(BadCredentialsException.class, () -> userService.login("test@mail.com", "wrong"));
	}

	@Test
	void getUserInfo_success() {
		User user = new User();
		user.setId(5);
		user.setEmail("test@mail.com");
		user.setUsername("john");

		when(userRepository.findById(5)).thenReturn(user);

		UserDto result = userService.getUserInfo(5);
		assertEquals("john", result.getUsername());
	}

	@Test
	void getUserInfo_notFound() {
		when(userRepository.findById(5)).thenReturn(null);

		assertThrows(UserNotFoundException.class, () -> userService.getUserInfo(5));
	}

	@Test
	void updateUser_success_username() {
		User user = new User();
		user.setId(1);
		user.setUsername("oldUser");

		UserDto dto = new UserDto();
		dto.setUsername("newUser");

		when(userRepository.findById(1)).thenReturn(user);
		when(userRepository.existsByUsername("newUser")).thenReturn(false);

		userService.updateUser(dto, 1);

		assertEquals("newUser", user.getUsername());
		verify(userRepository).save(user);
	}

	@Test
	void updateUser_usernameExists() {
		User user = new User();
		user.setId(1);

		UserDto dto = new UserDto();
		dto.setUsername("taken");

		when(userRepository.findById(1)).thenReturn(user);
		when(userRepository.existsByUsername("taken")).thenReturn(true);

		assertThrows(UsernameAlreadyExistsException.class, () -> userService.updateUser(dto, 1));
	}

	@Test
	void updateUser_emailExists() {
		User user = new User();
		user.setId(1);

		UserDto dto = new UserDto();
		dto.setEmail("taken@mail.com");

		when(userRepository.findById(1)).thenReturn(user);
		when(userRepository.existsByEmail("taken@mail.com")).thenReturn(true);

		assertThrows(EmailAlreadyExistsException.class, () -> userService.updateUser(dto, 1));
	}

	@Test
	void updateUser_userNotFound() {
		UserDto dto = new UserDto();
		dto.setUsername("newUser");

		when(userRepository.findById(1)).thenReturn(null);

		assertThrows(UserNotFoundException.class, () -> userService.updateUser(dto, 1));
	}

	@Test
	void deleteUser_success() {
		assertDoesNotThrow(() -> userService.deleteUser(10));
		verify(userRepository, times(1)).deleteById(10);
	}
}
