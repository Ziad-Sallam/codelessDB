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

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String username = oAuth2User.getAttribute("name");
        // String picture = oAuth2User.getAttribute("picture"); // profile picture URL,
        // need to get byte[] from this

        // if user exists, get user id
        User user = userService.findUserByEmail(email);

        if (user == null) { // create a new user
            UserDto userDto = new UserDto();
            userDto.setEmail(email);
            userDto.setRawPassword(UUID.randomUUID().toString());

            int attempt = new Random().nextInt(1, 100);
            while (userService.findUserByUsername(username) != null) {
                username = username + attempt;
                attempt++;
            }
            userDto.setUsername(username);

            userService.createUser(userDto);

        } else {
            username = user.getUsername();
        }

        String jwt = jwtUtil.generateToken(user.getId(), username);

        String redirectUrl = frontUrl + "/login/oauth2/google?token=" + jwt;
        response.sendRedirect(redirectUrl);
    }
}
