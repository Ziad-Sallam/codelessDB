package backend.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	
	@PostMapping("/signup")
	public ResponseEntity<?> signup() {
		return null;
		// Registration logic here
		// return ResponseEntity.ok("User registered");
	}
	
	@PostMapping("/login")
	public void login() {
		// Login logic here
	}

	@PutMapping("/update")
	public ResponseEntity<?> updateUser() {
		return null;
		// Update logic here
		// return ResponseEntity.ok("User updated");
	}

	@GetMapping("/info")
	public ResponseEntity<?> getUserInfo() {
		return null;
		// Retrieval logic here
		// return ResponseEntity.ok(userInfo);
	}

	@DeleteMapping("/delete")
	public ResponseEntity<?> deleteUser() {
		return null;
		// Deletion logic here
		// return ResponseEntity.ok("User deleted");
	}
}