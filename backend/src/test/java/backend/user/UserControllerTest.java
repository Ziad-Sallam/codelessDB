package backend.user;

import backend.entities.User;
import static backend.user.exceptions.UserException.*;
import backend.user.exceptions.UserExceptionHandler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserExceptionHandler.class)
public class UserControllerTest {

   @Autowired
   private MockMvc mockMvc;

   @SuppressWarnings("removal")
   @MockBean
   private UserService userService;

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

   @Nested
   @DisplayName("Signup API Tests")
   class SignupTests {

      @Test
      @DisplayName("Signup success")
      void testSignupSuccess() throws Exception {

         doNothing().when(userService).createUser(any(UserDto.class));

         UserDto dto = createDto("john", "john@example.com", "12345");

         mockMvc.perform(
               post("/user/signup")
                     .contentType(MediaType.APPLICATION_JSON)
                     .content(toJson(dto)))
               .andExpect(status().isOk())
               .andExpect(content().string("User registered"));
      }

      @Test
      @DisplayName("Signup fails: email exists")
      void testSignupEmailExists() throws Exception {

         doThrow(new EmailAlreadyExistsException("Email already exists"))
               .when(userService).createUser(any(UserDto.class));

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

         doThrow(new UsernameAlreadyExistsException("Username exists"))
               .when(userService).createUser(any(UserDto.class));

         UserDto dto = createDto("john", "john@example.com", "12345");

         mockMvc.perform(
               post("/user/signup")
                     .contentType(MediaType.APPLICATION_JSON)
                     .content(toJson(dto)))
               .andExpect(status().is4xxClientError());
      }

      @Test
      @DisplayName("Signup fails: missing field")
      void testSignupMissingField() throws Exception {

         doThrow(new IllegalArgumentException("Username, Password is required"))
               .when(userService).createUser(any(UserDto.class));

         UserDto dto = createDto(null, "john@example.com", null);

         mockMvc.perform(
               post("/user/signup")
                     .contentType(MediaType.APPLICATION_JSON)
                     .content(toJson(dto)))
               .andExpect(status().isBadRequest());
      }
   }

   @Nested
   @DisplayName("Login API Tests")
   class LoginTests {

      @Test
      @DisplayName("Login success")
      void testLoginSuccess() throws Exception {

         doNothing().when(userService).login("john@example.com", "12345");

         UserDto dto = createDto(null, "john@example.com", "12345");

         mockMvc.perform(
               post("/user/login")
                     .contentType(MediaType.APPLICATION_JSON)
                     .content(toJson(dto)))
               .andExpect(status().isOk());
      }

      @Test
      @DisplayName("Login fails: user not found")
      void testLoginUserNotFound() throws Exception {

         doThrow(new UserNotFoundException("User not found"))
               .when(userService).login(eq("ghost@example.com"), eq("12345"));

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

         doThrow(new BadCredentialsException("Invalid"))
               .when(userService).login(eq("john@example.com"), eq("wrong"));

         UserDto dto = createDto(null, "john@example.com", "wrong");

         mockMvc.perform(
               post("/user/login")
                     .contentType(MediaType.APPLICATION_JSON)
                     .content(toJson(dto)))
               .andExpect(status().is4xxClientError());
      }
   }

   @Nested
   @DisplayName("Get User Info Tests")
   class GetUserInfoTests {

      @Test
      @DisplayName("Get user info success")
      void testGetUserInfoSuccess() throws Exception {

         User user = new User();
         user.setUsername("john");
         user.setEmail("john@example.com");

         when(userService.getUserInfo(1)).thenReturn(new UserDto(user));

         mockMvc.perform(get("/user/info/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.username").value("john"))
               .andExpect(jsonPath("$.email").value("john@example.com"));
      }

      @Test
      @DisplayName("Get user info fails: user not found")
      void testGetUserNotFound() throws Exception {

         doThrow(new UserNotFoundException("User not found"))
               .when(userService).getUserInfo(99);

         mockMvc.perform(get("/user/info/99"))
               .andExpect(status().is4xxClientError());
      }
   }

   @Nested
   @DisplayName("Update User Tests")
   class UpdateTests {

      @Test
      @DisplayName("Update success")
      void testUpdateSuccess() throws Exception {

         doNothing().when(userService).updateUser(any(UserDto.class), eq(1));

         UserDto dto = new UserDto();
         dto.setUsername("newName");

         mockMvc.perform(
               put("/user/update/1")
                     .contentType(MediaType.APPLICATION_JSON)
                     .content(toJson(dto)))
               .andExpect(status().isOk())
               .andExpect(content().string("User updated"));
      }
   }

   @Nested
   @DisplayName("Delete User Tests")
   class DeleteTests {

      @Test
      @DisplayName("Delete success")
      void testDeleteSuccess() throws Exception {

         doNothing().when(userService).deleteUser(1);

         mockMvc.perform(delete("/user/delete/1"))
               .andExpect(status().isOk())
               .andExpect(content().string("User deleted"));
      }
   }
}
