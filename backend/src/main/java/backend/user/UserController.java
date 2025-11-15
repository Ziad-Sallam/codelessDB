package backend.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody UserDto userDto) {
		userService.createUser(userDto);
		return ResponseEntity.ok("User registered");
	}

	@PostMapping("/login")
	public void login(@RequestBody UserDto userDto) {
		userService.login(userDto.getEmail(), userDto.getRawPassword());
	}

	@GetMapping("/info/{id}")
	public ResponseEntity<?> getUserInfo(@PathVariable int id) { 
		//  id should get from token
		return ResponseEntity.ok(userService.getUserInfo(id));
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateUser(@RequestBody UserDto userDto, @PathVariable int id) {
		//  id should get from token
		userService.updateUser(userDto, id);
		return ResponseEntity.ok("User updated");
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable int id) {
		//  id should get from token
		userService.deleteUser(id);
		return ResponseEntity.ok("User deleted");
	}
}