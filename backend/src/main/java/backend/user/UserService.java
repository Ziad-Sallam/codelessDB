package backend.user;

import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import backend.entities.User;
import backend.user.exceptions.UserException.EmailAlreadyExistsException;
import backend.user.exceptions.UserException.InvalidEmailException;
import backend.user.exceptions.UserException.UserNotFoundException;
import backend.user.exceptions.UserException.UsernameAlreadyExistsException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

	@Autowired
	private UserRepository userRepository;

	private String encodePassword(String rawPassword) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder.encode(rawPassword);
	}

	public void createUser(UserDto userDto) throws RuntimeException {
		if (userDto.getEmail() == null) {
			throw new IllegalArgumentException("Email is required");
		}

		if (userDto.getUsername() == null) {
			throw new IllegalArgumentException("Username is required");
		}

		if (userDto.getRawPassword() == null) {
			throw new IllegalArgumentException("Password is required");
		}

		User newUser = new User();

		EmailValidator emailValidator = new EmailValidator();
		if (!emailValidator.isValid(userDto.getEmail(), null)) {
			throw new InvalidEmailException("Invalid email format");
		}

		if (userRepository.existsByEmail(userDto.getEmail())) {
			throw new EmailAlreadyExistsException("Email already exists");
		}

		if (userRepository.existsByUsername(userDto.getUsername())) {
			throw new UsernameAlreadyExistsException("Username already exists");
		}

		newUser.setEmail(userDto.getEmail());
		newUser.setUsername(userDto.getUsername());
		newUser.setPassword(encodePassword(userDto.getRawPassword()));
		userRepository.save(newUser);
	}

	public void login(String email, String rawPassword) throws RuntimeException {
		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new UserNotFoundException("User not found");
		}

		if (!new BCryptPasswordEncoder().matches(rawPassword, user.getPassword())) {
			throw new BadCredentialsException("Invalid credentials, Password mismatch");
		}

		// return JWT generation token
	}

	public UserDto getUserInfo(int id) throws RuntimeException {
		User user = userRepository.findById(id);
		if (user == null) {
			throw new UserNotFoundException("User not found");
		}

		return new UserDto(user);
	}

	public void updateUser(UserDto userDto, int id) throws RuntimeException {
		User user = userRepository.findById(id);
		if (user == null) {
			throw new UserNotFoundException("User not found");
		}

		if (userDto.getUsername() != null) {
			if (userRepository.existsByUsername(userDto.getUsername())) {
				throw new UsernameAlreadyExistsException("Username already exists");
			}
			user.setUsername(userDto.getUsername());

		} else if (userDto.getPicture() != null) {
			user.setPicture(userDto.getPicture());

		} else if (userDto.getRawPassword() != null) {
			user.setPassword(encodePassword(userDto.getRawPassword()));

		} else if (userDto.getEmail() != null) {
			if (userRepository.existsByEmail(userDto.getEmail())) {
				throw new EmailAlreadyExistsException("Email already exists");
			}
			user.setEmail(userDto.getEmail());
		}

		userRepository.save(user);
	}

	public void deleteUser(int id) throws RuntimeException {
		userRepository.deleteById(id);
		// diagrams (if the user is the only owner), servers deletion logic here
	}
}
