package backend.user;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.cloudinary.utils.ObjectUtils;

import backend.entities.User;
import backend.security.AuthUser;
import backend.security.JwtUtil;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private Cloudinary cloudinary;

	@Value("${cloudinary.upload_preset}")
	private String uploadPreset;

	@PostMapping("/signup/validate")
	public ResponseEntity<?> signupValidation(@RequestBody UserDto userDto) {
		try {
			userService.validateSignUp(userDto);
			return ResponseEntity.ok("Valid signup data");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody UserDto userDto) {
		int id = userService.createUser(userDto);
		return ResponseEntity.ok(jwtUtil.generateToken(id, userDto.getUsername()));
	}

	@PostMapping("/signup/send-otp/{email}")
	public ResponseEntity<?> sendOtp(@PathVariable String email) {
		String otp = userService.sendOtpEmail(email);
		return ResponseEntity.ok(otp);
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody UserDto userDto) {
		AuthUser user = userService.login(userDto.getEmail(), userDto.getRawPassword());
		String token = jwtUtil.generateToken(user.userId(), user.username());
		return ResponseEntity.ok(token);
	}

	@PostMapping("/login/forgot-password/{email}")
	public ResponseEntity<?> checkEmailExists(@PathVariable String email) {
		User user = userService.findUserByEmail(email);
		if (user == null) {
			return ResponseEntity.badRequest().body("Email does not exist");
		}
		return ResponseEntity.ok(jwtUtil.generateToken(user.getId(), user.getUsername()));
	}

	@GetMapping("/ay7aga")
	public ResponseEntity<?> login(@AuthenticationPrincipal AuthUser authUser) {
		UserDto userDto = userService.getUserInfo(authUser.userId());
		return ResponseEntity.ok(userDto);
	}

	@GetMapping("/info")
	public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal AuthUser authUser) {
		return ResponseEntity.ok(userService.getUserInfo(authUser.userId()));
	}

	@PutMapping("/update")
	public ResponseEntity<?> updateUser(@RequestBody UserDto userDto, @AuthenticationPrincipal AuthUser authUser) {
		userService.updateUser(userDto, authUser.userId());
		return ResponseEntity.ok("User updated");
	}

	@DeleteMapping("/delete")
	public ResponseEntity<?> deleteUser(@AuthenticationPrincipal AuthUser authUser) {
		userService.deleteUser(authUser.userId());
		return ResponseEntity.ok("User deleted");
	}

	@GetMapping("/signature/upload")
	public Map<String, Object> getSignature(@RequestParam String publicId) {
		long timestamp = System.currentTimeMillis() / 1000;

		Map<String, Object> paramsToSign = ObjectUtils.asMap(
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
