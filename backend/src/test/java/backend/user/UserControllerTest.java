package backend.user;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.cloudinary.Cloudinary;
import com.fasterxml.jackson.databind.ObjectMapper;

import backend.security.AuthUser;
import backend.security.JwtUtil;
import backend.user.exceptions.UserExceptionHandler;

@WebMvcTest({UserController.class, UserExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(UserControllerTest.TestConfig.class)
class UserControllerTest {

    @TestConfiguration
    static class TestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                        NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                    Principal principal = webRequest.getUserPrincipal();
                    if (principal instanceof UsernamePasswordAuthenticationToken auth) {
                        return auth.getPrincipal();
                    }
                    return null;
                }
            });
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean(name = "cloudinary")
    private Cloudinary cloudinary;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserController userController;

    @BeforeEach
    void setup() {
        com.cloudinary.Configuration config = new com.cloudinary.Configuration();
        config.apiKey = "key";
        config.apiSecret = "secret";
        config.cloudName = "cloud";
        ReflectionTestUtils.setField(cloudinary, "config", config);
        ReflectionTestUtils.setField(userController, "uploadPreset", "preset");
    }

    @Test
    void signupValidation_success() throws Exception {
        UserDto dto = new UserDto();
        dto.setEmail("test@mail.com");

        mockMvc.perform(post("/user/signup/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Valid signup data"));
    }

    @Test
    void signupValidation_failure() throws Exception {
        UserDto dto = new UserDto();
        doThrow(new RuntimeException("Error")).when(userService).validateSignUp(any());

        mockMvc.perform(post("/user/signup/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error"));
    }

    @Test
    void signup_success() throws Exception {
        UserDto dto = new UserDto();
        dto.setUsername("user");
        when(userService.createUser(any())).thenReturn(1);
        when(jwtUtil.generateToken(1, "user")).thenReturn("token");

        mockMvc.perform(post("/user/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("token"));
    }

    @Test
    void sendOtp_success() throws Exception {
        when(userService.sendOtpEmail("test@mail.com", "user")).thenReturn("12345");

        mockMvc.perform(post("/user/signup/send-otp/test@mail.com")
                .param("username", "user"))
                .andExpect(status().isOk())
                .andExpect(content().string("12345"));
    }

    @Test
    void login_success() throws Exception {
        UserDto dto = new UserDto();
        dto.setEmail("test@mail.com");
        dto.setRawPassword("pwd");
        when(userService.login("test@mail.com", "pwd")).thenReturn(new AuthUser(1, "user"));
        when(jwtUtil.generateToken(1, "user")).thenReturn("token");

        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("token"));
    }

    @Test
    void forgotPassword_success() throws Exception {
        backend.entities.User user = new backend.entities.User();
        user.setId(1);
        user.setUsername("user");
        when(userService.findUserByEmail("test@mail.com")).thenReturn(user);
        when(jwtUtil.generateToken(1, "user")).thenReturn("token");

        mockMvc.perform(post("/user/login/forgot-password/test@mail.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("token"));
    }

    @Test
    void forgotPassword_notFound() throws Exception {
        when(userService.findUserByEmail("test@mail.com")).thenReturn(null);

        mockMvc.perform(post("/user/login/forgot-password/test@mail.com"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email does not exist"));
    }

    @Test
    void getAuth_success() throws Exception {
        AuthUser authUser = new AuthUser(1, "user");
        UserDto dto = new UserDto();
        dto.setUsername("user");
        when(userService.getUserInfo(1)).thenReturn(dto);

        mockMvc.perform(get("/user/auth")
                .principal(new UsernamePasswordAuthenticationToken(authUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    void getInfo_success() throws Exception {
        AuthUser authUser = new AuthUser(1, "user");
        UserDto dto = new UserDto();
        dto.setUsername("user");
        when(userService.getUserInfo(1)).thenReturn(dto);

        mockMvc.perform(get("/user/info")
                .principal(new UsernamePasswordAuthenticationToken(authUser, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    void updateUser_success() throws Exception {
        UserDto dto = new UserDto();
        dto.setUsername("new");
        mockMvc.perform(put("/user/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
                .principal(new UsernamePasswordAuthenticationToken(new AuthUser(1, "user"), null)))
                .andExpect(status().isOk())
                .andExpect(content().string("User updated"));
    }

    @Test
    void deleteUser_success() throws Exception {
        mockMvc.perform(delete("/user/delete")
                .principal(new UsernamePasswordAuthenticationToken(new AuthUser(1, "user"), null)))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted"));
    }

    @Test
    void getSignature_success() throws Exception {
        when(cloudinary.apiSignRequest(anyMap(), any())).thenReturn("sig");

        mockMvc.perform(get("/user/signature/upload")
                .param("publicId", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.signature").value("sig"))
                .andExpect(jsonPath("$.apiKey").value("key"))
                .andExpect(jsonPath("$.cloudName").value("cloud"))
                .andExpect(jsonPath("$.uploadPreset").value("preset"));
    }
}
