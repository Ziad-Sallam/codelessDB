package backend.collab;

import backend.user.Role;
import lombok.Data;

@Data
public class Collaborator {
	private String username;
	private Role role;
}