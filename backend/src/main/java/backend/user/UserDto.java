package backend.user;

import java.sql.Date;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import backend.entities.User;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {
	private String username;
	private String email;

	@JsonProperty("password")
	private String rawPassword;
	
	private String picture;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdAt;

	public UserDto(User user) {
		this.username = user.getUsername();
		this.email = user.getEmail();
    	this.rawPassword = null;
		this.picture = user.getPicture();
		this.createdAt = user.getCreatedAt();
	}
}
