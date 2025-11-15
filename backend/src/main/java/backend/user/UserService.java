package backend.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import backend.entities.User;
import static backend.user.exceptions.UserException.*;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {

	@Autowired
	private UserRepository userRepository;

	private String encodePassword(String rawPassword) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder.encode(rawPassword);
	}

	public void createUser(UserDto userDto) throws RuntimeException {
		User newUser = new User();

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

	public void login(String email, String password) throws RuntimeException {
		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new UserNotFoundException("User not found");
		}

		password = encodePassword(password);
		if (!user.getPassword().matches(password)) {
			throw new BadCredentialsException("Invalid credentials");
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
