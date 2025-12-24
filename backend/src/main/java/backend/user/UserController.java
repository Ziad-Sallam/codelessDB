package backend.user;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cloudinary.Cloudinary;

import backend.entities.User;
import backend.config.ErrorResponse;
import backend.security.AuthUser;
import backend.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for user authentication, registration, and profile management")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final Cloudinary cloudinary;

    @Value("${cloudinary.upload_preset}")
    private String uploadPreset;

    @PostMapping("/signup/validate")
    @Operation(
            summary = "Validate signup data",
            description = "Checks if the provided email and username are available and valid"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Data is valid",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Valid Signup Example",
                            value = "\"Valid signup data\""
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Validation failed",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Email Exists",
                            value = "{\"message\": \"Email already exists\", \"status\": 400}"
                    )
            )
    )
    public ResponseEntity<?> signupValidation(@RequestBody UserDto userDto) {
        try {
            userService.validateSignUp(userDto);
            return ResponseEntity.ok("Valid signup data");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage(), "status", 400));
        }
    }

    @PostMapping("/signup")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns a JWT token"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User created successfully",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Signup JWT Example",
                            value = "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\""
                    )
            )
    )
    public ResponseEntity<?> signup(@RequestBody UserDto userDto) {
        int id = userService.createUser(userDto);
        return ResponseEntity.ok(jwtUtil.generateToken(id, userDto.getUsername()));
    }

    @PostMapping("/signup/send-otp/{email}")
    @Operation(
            summary = "Send OTP email",
            description = "Sends a verification code to the specified email address"
    )
    @ApiResponse(
            responseCode = "200",
            description = "OTP sent successfully",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "OTP Example",
                            value = "\"123456\""
                    )
            )
    )
    public ResponseEntity<?> sendOtp(
            @Parameter(description = "User's email address") @PathVariable String email,
            @Parameter(description = "Desired username") @RequestParam String username) {
        String otp = userService.sendOtpEmail(email, username);
        return ResponseEntity.ok(otp);
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates user and returns a JWT token"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Login JWT Example",
                            value = "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\""
                    )
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Invalid Login",
                            value = "{\"message\": \"Invalid email or password\", \"status\": 401}"
                    )
            )
    )
    public ResponseEntity<?> login(@RequestBody UserDto userDto) {
        AuthUser user = userService.login(userDto.getEmail(), userDto.getRawPassword());
        String token = jwtUtil.generateToken(user.userId(), user.username());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login/forgot-password/{email}")
    @Operation(
            summary = "Check email for password reset",
            description = "Verifies if an email exists in the system and returns a JWT token for password reset flow"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Email exists, token generated for password reset",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Password Reset Token Example",
                            value = "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\""
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Email does not exist",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Email Not Found",
                            value = "{\"message\": \"Email does not exist in the system\", \"status\": 400}"
                    )
            )
    )
    public ResponseEntity<?> checkEmailExists(@PathVariable String email) {
        User user = userService.findUserByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email does not exist", "status", 400));
        }
        return ResponseEntity.ok(jwtUtil.generateToken(user.getId(), user.getUsername()));
    }

    @GetMapping("/auth")
    @Operation(
            summary = "Authenticate current user",
            description = "Retrieves authenticated user information from the JWT token"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Returns authenticated user details",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class),
                    examples = @ExampleObject(
                            name = "Authenticated User Example",
                            value = """
                {
                  "id": 1,
                  "username": "johndoe",
                  "email": "johndoe@example.com",
                  "bio": "Software Developer",
                  "picture": "https://example.com/pic.jpg"
                }
                """
                    )
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Invalid or missing authentication token",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Unauthorized",
                            value = "{\"message\": \"Unauthorized\", \"status\": 401}"
                    )
            )
    )
    public ResponseEntity<?> login(@AuthenticationPrincipal AuthUser authUser) {
        UserDto userDto = userService.getUserInfo(authUser.userId());
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/info")
    @Operation(
            summary = "Get user profile information",
            description = "Retrieves detailed profile information for the authenticated user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Returns user profile data",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class),
                    examples = @ExampleObject(
                            name = "User Info Example",
                            value = """
                {
                  "id": 1,
                  "username": "johndoe",
                  "email": "johndoe@example.com",
                  "bio": "Software Developer",
                  "picture": "https://example.com/pic.jpg"
                }
                """
                    )
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "User not authenticated",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Unauthorized",
                            value = "{\"message\": \"Unauthorized\", \"status\": 401}"
                    )
            )
    )
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(userService.getUserInfo(authUser.userId()));
    }

    @PutMapping("/update")
    @Operation(
            summary = "Update user profile",
            description = "Updates user profile information including username, email, bio, and picture"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User profile updated successfully",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Update Success",
                            value = "\"User updated\""
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid data or duplicate username/email",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Update Error",
                            value = "{\"message\": \"Email already exists\", \"status\": 400}"
                    )
            )
    )
    public ResponseEntity<?> updateUser(@RequestBody UserDto userDto, @AuthenticationPrincipal AuthUser authUser) {
        userService.updateUser(userDto, authUser.userId());
        return ResponseEntity.ok("User updated");
    }

    @DeleteMapping("/delete")
    @Operation(
            summary = "Delete user account",
            description = "Permanently deletes the authenticated user's account and all associated data"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User account deleted successfully",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Delete Success",
                            value = "\"User deleted\""
                    )
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "User not authenticated",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Unauthorized",
                            value = "{\"message\": \"Unauthorized\", \"status\": 401}"
                    )
            )
    )
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal AuthUser authUser) {
        userService.deleteUser(authUser.userId());
        return ResponseEntity.ok("User deleted");
    }

    @GetMapping("/signature/upload")
    @Operation(
            summary = "Get Cloudinary upload signature",
            description = "Generates a signed request for uploading user profile pictures to Cloudinary CDN"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Returns signature, timestamp, API key, and upload configuration",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Cloudinary Upload Signature Example",
                            value = """
                {
                  "signature": "abc123signature",
                  "timestamp": 1713897600,
                  "apiKey": "1234567890",
                  "cloudName": "demo",
                  "uploadPreset": "user_upload_preset"
                }
                """
                    )
            )
    )
    public Map<String, Object> getSignature(@RequestParam String publicId) {
        long timestamp = System.currentTimeMillis() / 1000;

		Map<String, Object> paramsToSign = Map.of(
				"timestamp", timestamp,
				"upload_preset", uploadPreset,
				"public_id", publicId,
				"overwrite", true,
				"invalidate", true);

        String signature = cloudinary.apiSignRequest(paramsToSign, cloudinary.config.apiSecret);

		return Map.of(
				"signature", signature,
				"timestamp", timestamp,
				"apiKey", cloudinary.config.apiKey,
				"cloudName", cloudinary.config.cloudName,
				"uploadPreset", uploadPreset);
	}
}