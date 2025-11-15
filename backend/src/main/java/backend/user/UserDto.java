package backend.user;

import java.sql.Date;

import backend.entities.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {
	private String username;
	private String email;
	private String rawPassword;
	private byte[] picture;
	private Date createdAt;

	public UserDto(User user) {
		this.username = user.getUsername();
		this.email = user.getEmail();
    	this.rawPassword = null;
		this.picture = user.getPicture();
		this.createdAt = user.getCreatedAt();
	}
}
