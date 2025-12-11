package backend.user;

import java.sql.Date;
import java.time.LocalDate;

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
	private Date createdAt;
	private int aiQuotaRemaining;
	private LocalDate aiQuotaResetDate;

	public UserDto(User user) {
		this.username = user.getUsername();
		this.email = user.getEmail();
		this.rawPassword = null;
		this.picture = user.getPicture();
		this.createdAt = user.getCreatedAt();
		this.aiQuotaRemaining = user.getAiQuotaRemaining();
		this.aiQuotaResetDate = user.getAiQuotaResetDate();
	}
}
