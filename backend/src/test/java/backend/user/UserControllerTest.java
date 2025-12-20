package backend.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.cloudinary.Cloudinary;

import backend.security.JwtUtil;
import backend.user.exceptions.UserExceptionHandler;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserExceptionHandler.class)
@TestPropertySource(properties = {
        "cloudinary.upload_preset=test-preset"
})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private Cloudinary cloudinary;

    @Test
    void sendOtp_success_withUsername() throws Exception {
        String email = "test@example.com";
        String username = "testuser";
        String expectedOtp = "12345";

        when(userService.sendOtpEmail(email, username)).thenReturn(expectedOtp);

        mockMvc.perform(post("/user/signup/send-otp/" + email)
                .param("username", username))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedOtp));
    }

    @Test
    void sendOtp_failure_missingUsername() throws Exception {
        String email = "test@example.com";

        mockMvc.perform(post("/user/signup/send-otp/" + email))
                .andExpect(status().isBadRequest());
    }
}
