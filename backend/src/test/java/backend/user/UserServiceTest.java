package backend.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import backend.entities.User;
import backend.user.exceptions.UserException;
import backend.user.exceptions.UserException.EmailAlreadyExistsException;
import backend.user.exceptions.UserException.InvalidEmailException;
import backend.user.exceptions.UserException.OtpSendFailedException;
import backend.user.exceptions.UserException.UserNotFoundException;
import backend.user.exceptions.UserException.UsernameAlreadyExistsException;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // --- createUser tests ---

    @Test
    void createUser_success() {
        UserDto dto = new UserDto();
        dto.setEmail("test@mail.com");
        dto.setUsername("john");
        dto.setRawPassword("123");

        when(userRepository.existsByEmail("test@mail.com")).thenReturn(false);
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("123")).thenReturn("encoded_123");

        userService.createUser(dto);

        verify(userRepository, times(1)).save(argThat(user -> 
            user.getEmail().equals("test@mail.com") &&
            user.getUsername().equals("john") &&
            user.getPassword().equals("encoded_123")
        ));
    }

    @Test
    void createUser_success_withPicture() {
        UserDto dto = new UserDto();
        dto.setEmail("test@mail.com");
        dto.setUsername("john");
        dto.setRawPassword("123");
        dto.setPicture("profile.jpg");

        when(userRepository.existsByEmail("test@mail.com")).thenReturn(false);
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("123")).thenReturn("encoded_123");

        userService.createUser(dto);

        verify(userRepository).save(argThat(user -> 
            user.getPicture().equals("profile.jpg")
        ));
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
    void createUser_emptyUsername() {
        UserDto dto = new UserDto();
        dto.setEmail("test@mail.com");
        dto.setUsername("   ");
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

    // --- login tests ---

    @Test
    void login_success() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@mail.com");
        user.setUsername("john");
        user.setPassword("encoded");

        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(passwordEncoder.matches("123", "encoded")).thenReturn(true);

        var authUser = userService.login("test@mail.com", "123");
        assertEquals(1, authUser.userId());
        assertEquals("john", authUser.username());
    }

    @Test
    void login_userNotFound() {
        when(userRepository.findByEmail("test@mail.com")).thenReturn(null);
        assertThrows(UserNotFoundException.class, () -> userService.login("test@mail.com", "123"));
    }

    @Test
    void login_invalidPassword() {
        User user = new User();
        user.setPassword("encoded");
        when(userRepository.findByEmail("test@mail.com")).thenReturn(user);
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);
        assertThrows(BadCredentialsException.class, () -> userService.login("test@mail.com", "wrong"));
    }

    // --- getUserInfo tests ---

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

    // --- getUserOrThrow tests ---

    @Test
    void getUserOrThrow_success() {
        User user = new User();
        when(userRepository.findById(1)).thenReturn(user);
        assertEquals(user, userService.getUserOrThrow(1));
    }

    @Test
    void getUserOrThrow_notFound() {
        when(userRepository.findById(1)).thenReturn(null);
        assertThrows(UserNotFoundException.class, () -> userService.getUserOrThrow(1));
    }

    // --- updateUser tests ---

    @Test
    void updateUser_success_username() {
        User user = new User();
        user.setId(1);
        user.setUsername("old");
        when(userRepository.findById(1)).thenReturn(user);
        when(userRepository.existsByUsername("new")).thenReturn(false);

        UserDto dto = new UserDto();
        dto.setUsername("new");
        userService.updateUser(dto, 1);
        assertEquals("new", user.getUsername());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_usernameExists() {
        User user = new User();
        when(userRepository.findById(1)).thenReturn(user);
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        UserDto dto = new UserDto();
        dto.setUsername("taken");
        assertThrows(UsernameAlreadyExistsException.class, () -> userService.updateUser(dto, 1));
    }

    @Test
    void updateUser_success_picture() {
        User user = new User();
        when(userRepository.findById(1)).thenReturn(user);
        UserDto dto = new UserDto();
        dto.setPicture("img.png");
        userService.updateUser(dto, 1);
        assertEquals("img.png", user.getPicture());
    }

    @Test
    void updateUser_success_password() {
        User user = new User();
        when(userRepository.findById(1)).thenReturn(user);
        when(passwordEncoder.encode("newpwd")).thenReturn("encoded");
        UserDto dto = new UserDto();
        dto.setRawPassword("newpwd");
        userService.updateUser(dto, 1);
        assertEquals("encoded", user.getPassword());
    }

    @Test
    void updateUser_success_email() {
        User user = new User();
        when(userRepository.findById(1)).thenReturn(user);
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
        UserDto dto = new UserDto();
        dto.setEmail("new@mail.com");
        userService.updateUser(dto, 1);
        assertEquals("new@mail.com", user.getEmail());
    }

    @Test
    void updateUser_emailExists() {
        User user = new User();
        when(userRepository.findById(1)).thenReturn(user);
        when(userRepository.existsByEmail("taken@mail.com")).thenReturn(true);
        UserDto dto = new UserDto();
        dto.setEmail("taken@mail.com");
        assertThrows(EmailAlreadyExistsException.class, () -> userService.updateUser(dto, 1));
    }

    @Test
    void updateUser_success_bio() {
        User user = new User();
        when(userRepository.findById(1)).thenReturn(user);
        UserDto dto = new UserDto();
        dto.setBio("my bio");
        userService.updateUser(dto, 1);
        assertEquals("my bio", user.getBio());
    }

    @Test
    void updateUser_success_publicProfile() {
        User user = new User();
        when(userRepository.findById(1)).thenReturn(user);
        UserDto dto = new UserDto();
        dto.setPublicProfile("profile");
        userService.updateUser(dto, 1);
        assertEquals("profile", user.getPublicProfile());
    }

    @Test
    void updateUser_success_website() {
        User user = new User();
        when(userRepository.findById(1)).thenReturn(user);
        UserDto dto = new UserDto();
        dto.setProfileWebsiteUrl("https://site.com");
        userService.updateUser(dto, 1);
        assertEquals("https://site.com", user.getProfileWebsiteUrl());
    }

    @Test
    void updateUser_userNotFound() {
        when(userRepository.findById(1)).thenReturn(null);
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(new UserDto(), 1));
    }

    // --- validateSignUp tests ---

    @Test
    void validateSignUp_emailExists() {
        UserDto dto = new UserDto();
        dto.setEmail("exists@mail.com");
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(new User());
        assertThrows(EmailAlreadyExistsException.class, () -> userService.validateSignUp(dto));
    }

    @Test
    void validateSignUp_usernameExists() {
        UserDto dto = new UserDto();
        dto.setUsername("exists");
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(new User());
        assertThrows(UsernameAlreadyExistsException.class, () -> userService.validateSignUp(dto));
    }

    @Test
    void validateSignUp_success() {
        UserDto dto = new UserDto();
        dto.setEmail("new@mail.com");
        dto.setUsername("new");
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        assertDoesNotThrow(() -> userService.validateSignUp(dto));
    }

    // --- sendOtpEmail tests ---

    @Test
    void sendOtpEmail_success_explicitUsername() {
        String email = "test@mail.com";
        String explicitUsername = "CustomUser";
        jakarta.mail.internet.MimeMessage mimeMessage = mock(jakarta.mail.internet.MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        String result = userService.sendOtpEmail(email, explicitUsername);

        assertNotNull(result);
        assertEquals(5, result.length());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendOtpEmail_success_implicitUsername() {
        String email = "test@mail.com";
        User user = new User();
        user.setUsername("DbUser");
        when(userRepository.findByEmail(email)).thenReturn(user);
        jakarta.mail.internet.MimeMessage mimeMessage = mock(jakarta.mail.internet.MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        String result = userService.sendOtpEmail(email, null);

        assertNotNull(result);
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendOtpEmail_failure() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Error"));
        assertThrows(OtpSendFailedException.class, () -> userService.sendOtpEmail("test@mail.com", "user"));
    }

    // --- AI Quota tests ---

    @Test
    void getAiQuota_userNotFound() {
        when(userRepository.findById(1)).thenReturn(null);
        assertThrows(UserNotFoundException.class, () -> userService.getAiQuota(1));
    }

    @Test
    void getAiQuota_reset_whenDateNull() {
        User user = new User();
        user.setAiQuotaRemaining(0);
        user.setAiQuotaResetDate(null);
        when(userRepository.findById(1)).thenReturn(user);

        int quota = userService.getAiQuota(1);

        assertEquals(5, quota);
        assertNotNull(user.getAiQuotaResetDate());
        verify(userRepository).save(user);
    }

    @Test
    void getAiQuota_reset_whenDateDifferent() {
        User user = new User();
        user.setAiQuotaRemaining(0);
        user.setAiQuotaResetDate(LocalDateTime.now().minusDays(1));
        when(userRepository.findById(1)).thenReturn(user);

        int quota = userService.getAiQuota(1);

        assertEquals(5, quota);
        verify(userRepository).save(user);
    }

    @Test
    void checkAndDecrementAiQuota_success() {
        User user = new User();
        user.setAiQuotaRemaining(5);
        user.setAiQuotaResetDate(LocalDateTime.now());
        when(userRepository.findById(1)).thenReturn(user);

        userService.checkAndDecrementAiQuota(1);

        assertEquals(4, user.getAiQuotaRemaining());
        verify(userRepository, atLeastOnce()).save(user);
    }

    @Test
    void checkAndDecrementAiQuota_quotaExceeded() {
        User user = new User();
        user.setAiQuotaRemaining(0);
        user.setAiQuotaResetDate(LocalDateTime.now());
        when(userRepository.findById(1)).thenReturn(user);

        assertThrows(UserException.QuotaExceededException.class, () -> userService.checkAndDecrementAiQuota(1));
    }

    @Test
    void checkAndDecrementAiQuota_userNotFound() {
        when(userRepository.findById(1)).thenReturn(null);
        assertThrows(UserNotFoundException.class, () -> userService.checkAndDecrementAiQuota(1));
    }

    // --- deleteUser tests ---

    @Test
    void deleteUser_success() {
        userService.deleteUser(1);
        verify(userRepository).deleteById(1);
    }
}
