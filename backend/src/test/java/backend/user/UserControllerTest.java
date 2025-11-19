package backend.user;

import backend.entities.User;
import backend.security.AuthUser;
import backend.security.JwtUtil;
import backend.user.exceptions.UserException.EmailAlreadyExistsException;
import backend.user.exceptions.UserException.UserNotFoundException;
import backend.user.exceptions.UserException.UsernameAlreadyExistsException;
import backend.user.exceptions.UserExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserExceptionHandler.class)
public class UserControllerTest {

      @Autowired
      private MockMvc mockMvc;

      @MockitoBean
      private UserService userService;

      @MockitoBean
      private JwtUtil jwtUtil;

      private static final ObjectMapper mapper = new ObjectMapper();

      private String toJson(Object obj) throws Exception {
            return mapper.writeValueAsString(obj);
      }

      private UserDto createDto(String username, String email, String password) {
            UserDto dto = new UserDto();
            dto.setUsername(username);
            dto.setEmail(email);
            dto.setRawPassword(password);
            return dto;
      }

      private void setAuthentication(AuthUser authUser) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(authUser, null,
                        null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      private void clearAuthentication() {
            SecurityContextHolder.clearContext();
      }

      // ---------------- Signup tests ----------------
      @Nested
      @DisplayName("Signup API Tests")
      class SignupTests {

            @Test
            @DisplayName("Signup success")
            void testSignupSuccess() throws Exception {
                  when(userService.createUser(any(UserDto.class))).thenReturn(10);
                  when(jwtUtil.generateToken(10, "john")).thenReturn("fake-token");

                  UserDto dto = createDto("john", "john@example.com", "12345");

                  mockMvc.perform(
                              post("/user/signup")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(toJson(dto)))
                              .andExpect(status().isOk())
                              .andExpect(content().string("fake-token"));
            }

            @Test
            @DisplayName("Signup fails: email exists")
            void testSignupEmailExists() throws Exception {
                  when(userService.createUser(any(UserDto.class)))
                              .thenThrow(new EmailAlreadyExistsException("Email already exists"));

                  UserDto dto = createDto("john", "john@example.com", "12345");

                  mockMvc.perform(
                              post("/user/signup")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(toJson(dto)))
                              .andExpect(status().is4xxClientError());
            }

            @Test
            @DisplayName("Signup fails: username exists")
            void testSignupUsernameExists() throws Exception {
                  when(userService.createUser(any(UserDto.class)))
                              .thenThrow(new UsernameAlreadyExistsException("Username exists"));

                  UserDto dto = createDto("john", "john@example.com", "12345");

                  mockMvc.perform(
                              post("/user/signup")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(toJson(dto)))
                              .andExpect(status().is4xxClientError());
            }

            @Test
            @DisplayName("Signup fails: missing fields")
            void testSignupMissingField() throws Exception {
                  when(userService.createUser(any(UserDto.class)))
                              .thenThrow(new IllegalArgumentException("Username, Password required"));

                  UserDto dto = createDto(null, "john@example.com", null);

                  mockMvc.perform(
                              post("/user/signup")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(toJson(dto)))
                              .andExpect(status().isBadRequest());
            }
      }

      // ---------------- Login tests ----------------
      @Nested
      @DisplayName("Login API Tests")
      class LoginTests {

            @Test
            @DisplayName("Login success")
            void testLoginSuccess() throws Exception {
                  AuthUser mockAuth = new AuthUser(1, "john");
                  when(userService.login("john@example.com", "12345")).thenReturn(mockAuth);
                  when(jwtUtil.generateToken(1, "john")).thenReturn("login-token");

                  UserDto dto = createDto(null, "john@example.com", "12345");

                  mockMvc.perform(
                              post("/user/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(toJson(dto)))
                              .andExpect(status().isOk())
                              .andExpect(content().string("login-token"));
            }

            @Test
            @DisplayName("Login fails: user not found")
            void testLoginUserNotFound() throws Exception {
                  when(userService.login(eq("ghost@example.com"), eq("12345")))
                              .thenThrow(new UserNotFoundException("User not found"));

                  UserDto dto = createDto(null, "ghost@example.com", "12345");

                  mockMvc.perform(
                              post("/user/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(toJson(dto)))
                              .andExpect(status().is4xxClientError());
            }

            @Test
            @DisplayName("Login fails: wrong password")
            void testLoginWrongPassword() throws Exception {
                  when(userService.login(eq("john@example.com"), eq("wrong")))
                              .thenThrow(new BadCredentialsException("Invalid"));

                  UserDto dto = createDto(null, "john@example.com", "wrong");

                  mockMvc.perform(
                              post("/user/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(toJson(dto)))
                              .andExpect(status().is4xxClientError());
            }
      }

      // ---------------- Get user info ----------------
      @Nested
      @DisplayName("Authenticated Get User Info")
      class InfoTests {

            @Test
            @DisplayName("Get info success")
            void testGetInfoSuccess() throws Exception {
                  User user = new User();
                  user.setUsername("john");
                  user.setEmail("john@example.com");

                  when(userService.getUserInfo(1)).thenReturn(new UserDto(user));

                  AuthUser auth = new AuthUser(1, "john");
                  setAuthentication(auth);

                  try {
                        mockMvc.perform(
                                    get("/user/info"))
                                    .andExpect(status().isOk())
                                    .andExpect(jsonPath("$.username").value("john"))
                                    .andExpect(jsonPath("$.email").value("john@example.com"));
                  } finally {
                        clearAuthentication();
                  }
            }

            @Test
            @DisplayName("Get info fails: user not found")
            void testGetInfoNotFound() throws Exception {
                  when(userService.getUserInfo(99)).thenThrow(new UserNotFoundException("User not found"));

                  AuthUser auth = new AuthUser(99, "ghost");
                  setAuthentication(auth);

                  try {
                        mockMvc.perform(
                                    get("/user/info"))
                                    .andExpect(status().is4xxClientError());
                  } finally {
                        clearAuthentication();
                  }
            }
      }

      // ---------------- Update user ----------------
      @Nested
      @DisplayName("Update user")
      class UpdateTests {

            @Test
            @DisplayName("Update success")
            void testUpdateSuccess() throws Exception {
                  UserDto dto2 = new UserDto();
                  dto2.setUsername("newName");

                  doNothing().when(userService).updateUser(any(UserDto.class), eq(1));

                  AuthUser auth = new AuthUser(1, "john");
                  setAuthentication(auth);

                  try {
                        mockMvc.perform(
                                    put("/user/update")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(toJson(dto2)))
                                    .andExpect(status().isOk())
                                    .andExpect(content().string("User updated"));
                  } finally {
                        clearAuthentication();
                  }
            }

            @Test
            @DisplayName("Update fails: username exists")
            void testUpdateUsernameExists() throws Exception {
                  UserDto dto2 = new UserDto();
                  dto2.setUsername("taken");

                  doThrow(new UsernameAlreadyExistsException("Username exists"))
                              .when(userService).updateUser(any(UserDto.class), eq(1));

                  AuthUser auth = new AuthUser(1, "john");
                  setAuthentication(auth);

                  try {
                        mockMvc.perform(
                                    put("/user/update")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(toJson(dto2)))
                                    .andExpect(status().is4xxClientError());
                  } finally {
                        clearAuthentication();
                  }
            }
      }

      // ---------------- Delete user ----------------
      @Nested
      @DisplayName("Delete user")
      class DeleteTests {

            @Test
            @DisplayName("Delete success")
            void testDeleteSuccess() throws Exception {
                  doNothing().when(userService).deleteUser(1);

                  AuthUser auth = new AuthUser(1, "john");
                  setAuthentication(auth);

                  try {
                        mockMvc.perform(
                                    delete("/user/delete"))
                                    .andExpect(status().isOk())
                                    .andExpect(content().string("User deleted"));
                  } finally {
                        clearAuthentication();
                  }
            }

            @Test
            @DisplayName("Delete fails: repository error (mapped by handler)")
            void testDeleteFails() throws Exception {
                  doThrow(new RuntimeException("db error")).when(userService).deleteUser(1);

                  AuthUser auth = new AuthUser(1, "john");
                  setAuthentication(auth);

                  try {
                        mockMvc.perform(
                                    delete("/user/delete"))
                                    .andExpect(status().is5xxServerError());
                  } finally {
                        clearAuthentication();
                  }
            }
      }
}