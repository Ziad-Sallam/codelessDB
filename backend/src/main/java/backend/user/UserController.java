package backend.user;

import backend.security.AuthUser;
import backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto userDto) {
        AuthUser user = userService.login(userDto.getEmail(), userDto.getRawPassword());
        String token = jwtUtil.generateToken(user.userId(), user.username());
        return ResponseEntity.ok(token);
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
