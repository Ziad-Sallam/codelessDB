package backend.security;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import backend.entities.User;
import backend.user.UserDto;
import backend.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GoogleSuccessHandler implements AuthenticationSuccessHandler {

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private UserService userService;

  @Value("${frontend.url}")
  private String frontUrl;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException {

    log.info("Request URL: {}", request.getRequestURL());
    log.info("Request URI: {}", request.getRequestURI());

    try {
      OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

      String email = oAuth2User.getAttribute("email");
      String username = oAuth2User.getAttribute("name");
      String picture = oAuth2User.getAttribute("picture");

      log.info("=== Google OAuth Success ===");
      log.info("Email: {}", email);
      log.info("Username: {}", username);
      log.info("Request URI: {}", request.getRequestURI());

      // Check if user exists
      User user = userService.findUserByEmail(email);

      if (user == null) {
        // Create a new user
        log.info("Creating new user for email: {}", email);

        UserDto userDto = new UserDto();
        userDto.setEmail(email);
        userDto.setRawPassword(UUID.randomUUID().toString());

        // Ensure unique username
        String uniqueUsername = username;
        int attempt = new Random().nextInt(1, 100);
        while (userService.findUserByUsername(uniqueUsername) != null) {
          uniqueUsername = username + attempt;
          attempt++;
        }
        userDto.setUsername(uniqueUsername);

        // Create user and get the userId
        int userId = userService.createUser(userDto);
        log.info("New user created with ID: {}", userId);

        // Fetch the created user from database
        user = userService.findUserByEmail(email);

      } else {
        log.info("Existing user found with ID: {}", user.getId());
      }

      // Generate JWT token
      String jwt = jwtUtil.generateToken(user.getId(), user.getUsername());

      // Redirect to frontend with token
      String redirectUrl = frontUrl + "/login?token=" + jwt;

      log.info("Redirecting to: {}", redirectUrl);
      response.sendRedirect(redirectUrl);

    } catch (Exception e) {
      log.error("Error in Google OAuth authentication", e);
      response.sendRedirect(frontUrl + "/login?error=oauth_failed");
    }
  }
}