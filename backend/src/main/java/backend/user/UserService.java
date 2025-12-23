package backend.user;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.entities.User;
import backend.security.AuthUser;
import backend.user.exceptions.UserException;
import backend.user.exceptions.UserException.EmailAlreadyExistsException;
import backend.user.exceptions.UserException.InvalidEmailException;
import backend.user.exceptions.UserException.OtpSendFailedException;
import backend.user.exceptions.UserException.UserNotFoundException;
import backend.user.exceptions.UserException.UsernameAlreadyExistsException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	private final JavaMailSender mailSender;

	private final PasswordEncoder passwordEncoder;

	@org.springframework.beans.factory.annotation.Value("${frontend.url}")
	private String frontendUrl;

	private String encodePassword(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	@Transactional
	public int createUser(UserDto userDto) throws RuntimeException {
		if (userDto.getEmail() == null) {
			throw new IllegalArgumentException("Email is required");
		}

		if (userDto.getUsername() == null) {
			throw new IllegalArgumentException("Username is required");
		}

		if (userDto.getUsername().trim().isEmpty()) {
			throw new IllegalArgumentException("Username cannot be empty");
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

		if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
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

	public String sendOtpEmail(String email, String explicitUsername) {
		try {
			String otp = String.format("%05d", (int) (Math.random() * 100000));

			String username = "User";

			if (explicitUsername != null && !explicitUsername.trim().isEmpty()) {
				username = explicitUsername;
			} else {
				User user = userRepository.findByEmail(email);
				if (user != null) {
					username = user.getUsername();
				}
			}

			jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
			org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(
					message, true, "UTF-8");

			helper.setFrom("codelessDB@no-reply.com");
			helper.setTo(email);
			helper.setSubject("Your Verification Code: " + otp);

			String content = String.format(
					"<div style=\"font-family: Arial, sans-serif; padding: 20px; color: #333; max-width: 600px; border: 1px solid #eee; border-radius: 10px;\">"
							+
							"<h2>Hello %s,</h2>" +
							"<p>Your verification code for CodelessDB is:</p>" +
							"<h1 style=\"color: #000000ff; font-size: 32px; letter-spacing: 5px; user-select: all; -webkit-user-select: all; -moz-user-select: all; background: #f9f9f9; padding: 10px; border-radius: 5px; display: inline-block;\">%s</h1>"
							+
							"<p>This code will expire in 5 minutes.</p>" +
							"</div>",
					username, otp);

			helper.setText(content, true);

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

		if (user.getAiQuotaResetDate() == null || !user.getAiQuotaResetDate().toLocalDate().isEqual(LocalDate.now())) {
			user.setAiQuotaRemaining(5);
			user.setAiQuotaResetDate(LocalDateTime.now());
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

	public Page<UserSearchDto> searchUsers(String query, int page, int size, Integer excludeDatabaseId) {
		Pageable pageable = PageRequest.of(page, size);
		Page<User> results;

		if (excludeDatabaseId != null) {
			results = userRepository.searchUsersExcludingDatabase(query, excludeDatabaseId, pageable);
		} else {
			results = userRepository.simpleSearchUsers(query, pageable);
		}

		return results.map(UserSearchDto::new);
	}
}
