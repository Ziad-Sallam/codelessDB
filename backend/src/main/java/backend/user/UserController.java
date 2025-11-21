package backend.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import backend.entities.User;
import backend.security.AuthUser;
import backend.security.JwtUtil;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private JwtUtil jwtUtil;

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody UserDto userDto) {
		int id = userService.createUser(userDto);
		return ResponseEntity.ok(jwtUtil.generateToken(id, userDto.getUsername()));
	}

	@PostMapping("/signup/validate")
	public ResponseEntity<?> signupValidation(@RequestBody UserDto userDto) {
		try {
			userService.validateSignUp(userDto);
			return ResponseEntity.ok("Valid signup data");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody UserDto userDto) {
		AuthUser user = userService.login(userDto.getEmail(), userDto.getRawPassword());
		String token = jwtUtil.generateToken(user.userId(), user.username());
		return ResponseEntity.ok(token);
	}

	@PostMapping("/login/forgot-password")
	public ResponseEntity<?> checkEmailExists(@RequestBody UserDto userDto) {
		User user = userService.findUserByEmail(userDto.getEmail());
		if (user == null) {
			return ResponseEntity.badRequest().body("Email does not exist");
		}
		return ResponseEntity.ok(jwtUtil.generateToken(user.getId(), user.getUsername()));
	}

	@GetMapping("/login")
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
}
