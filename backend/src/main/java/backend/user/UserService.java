package backend.user;

import backend.user.exceptions.UserException;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.entities.User;
import backend.security.AuthUser;
import backend.user.exceptions.UserException;
import backend.user.exceptions.UserException.EmailAlreadyExistsException;
import backend.user.exceptions.UserException.InvalidEmailException;
import backend.user.exceptions.UserException.UserNotFoundException;
import backend.user.exceptions.UserException.UsernameAlreadyExistsException;
import backend.user.exceptions.UserException.OtpSendFailedException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JavaMailSender mailSender;

	private String encodePassword(String rawPassword) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder.encode(rawPassword);
	}

	@Transactional
	public int createUser(UserDto userDto) throws RuntimeException {
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
		newUser.setPublicProfile(userDto.getUsername());
		

		if (userDto.getPicture() != null) {
			newUser.setPicture(userDto.getPicture());
		}
		userRepository.save(newUser);

		return newUser.getId();
	}

	@Transactional
	public AuthUser login(String email, String rawPassword) throws RuntimeException {
		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new UserNotFoundException("User not found");
		}

		if (!new BCryptPasswordEncoder().matches(rawPassword, user.getPassword())) {
			throw new BadCredentialsException("Invalid credentials, Password mismatch");
		}

		return new AuthUser(user.getId(), user.getUsername());
	}

	public UserDto getUserInfo(int id) throws RuntimeException {
		User user = userRepository.findById(id);
		if (user == null) {
			throw new UserNotFoundException("User not found");
		}

		return new UserDto(user);
	}

	@Transactional
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

		} else if (userDto.getBio() != null) {
			user.setBio(userDto.getBio());

		} else if (userDto.getPublicProfile() != null) {
			user.setPublicProfile(userDto.getPublicProfile());

		} else if (userDto.getProfileWebsiteUrl() != null) {
			user.setProfileWebsiteUrl(userDto.getProfileWebsiteUrl());
		}

		userRepository.save(user);
	}

	@Transactional
	public void deleteUser(int id) throws RuntimeException {
		userRepository.deleteById(id);
		// diagrams (if the user is the only owner), servers deletion logic here
	}

	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public User findUserByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	public void validateSignUp(UserDto userDto) {
		User userByEmail = findUserByEmail(userDto.getEmail());
		if (userByEmail != null) {
			throw new EmailAlreadyExistsException("Email already exists");
		}

		User userByUsername = findUserByUsername(userDto.getUsername());
		if (userByUsername != null) {
			throw new UsernameAlreadyExistsException("Username already exists");
		}
	}

	public String sendOtpEmail(String email) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			String otp = String.format("%05d", (int) (Math.random() * 100000));

			message.setFrom("legendboudy@gmail.com");
			message.setTo(email);
			message.setSubject("Your Password Reset OTP");
			message.setText("Your OTP is: " + otp + "");

			mailSender.send(message);

			return otp;

		} catch (Exception e) {
			throw new OtpSendFailedException("Failed to send OTP. Please try again.");
		}
	}

	public User getUserOrThrow(int userId) {
		User user = userRepository.findById(userId);

		if (user == null) {
			throw new UserNotFoundException("User not found with id: " + userId);
		}

		return user;
	}

	/**
	 * Reset AI quota to 5 if the date has changed since last reset
	 */
	private void resetAiQuotaIfNeeded(User user) {
		java.time.LocalDate today = java.time.LocalDate.now();

		if (user.getAiQuotaResetDate() == null || !user.getAiQuotaResetDate().equals(today)) {
			user.setAiQuotaRemaining(5);
			user.setAiQuotaResetDate(today);
			userRepository.save(user);
		}
	}

	/**
	 * Check if user has AI quota remaining, reset if needed, and decrement quota
	 * 
	 * @param userId User ID
	 * @throws UserException.QuotaExceededException if quota is 0
	 */
	@Transactional
	public void checkAndDecrementAiQuota(int userId) {
		User user = userRepository.findById(userId);
		if (user == null) {
			throw new UserNotFoundException("User not found");
		}

		// Reset quota if date has changed
		resetAiQuotaIfNeeded(user);

		// Check if quota is available
		if (user.getAiQuotaRemaining() <= 0) {
			throw new UserException.QuotaExceededException(
					"Daily AI quota exceeded. You have 0 requests remaining. Quota resets at midnight.");
		}

		// Decrement quota
		user.setAiQuotaRemaining(user.getAiQuotaRemaining() - 1);
		userRepository.save(user);
	}

	/**
	 * Get current AI quota for a user (with reset check)
	 */
	public int getAiQuota(int userId) {
		User user = userRepository.findById(userId);
		if (user == null) {
			throw new UserNotFoundException("User not found");
		}

		resetAiQuotaIfNeeded(user);
		return user.getAiQuotaRemaining();
	}
}
