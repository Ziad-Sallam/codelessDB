package backend.user;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import backend.entities.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "User Data Transfer Object")
public class UserDto {
	@Schema(description = "Unique username for the user", example = "johndoe")
	private String username;
	
	@Schema(description = "Valid email address", example = "john@example.com")
	private String email;

	@JsonProperty("password")
	@Schema(description = "User password (only for creation/login)", example = "P@ssw0rd123", accessMode = Schema.AccessMode.WRITE_ONLY)
	private String rawPassword;

	@Schema(description = "Profile picture URL", example = "http://res.cloudinary.com/...")
	private String picture;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Schema(description = "Account creation timestamp", example = "2023-12-24 10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
	private LocalDateTime createdAt;

	@Schema(description = "User's biography", example = "Expert database architect")
	private String bio;

	@Schema(description = "Link to the user's public profile", example = "johndoe-profile")
	private String publicProfile;

	@Schema(description = "User's personal website URL", example = "https://johndoe.me")
	private String profileWebsiteUrl;

	@Schema(description = "Remaining AI quota for SQL optimization", example = "50")
	private int aiQuotaRemaining;

	@Schema(description = "The timestamp when the AI quota will reset")
	private LocalDateTime aiQuotaResetDate;

    @Schema(description = "Number of followers", example = "10")
    private int followersCount;

    @Schema(description = "Number of users followed by this user", example = "15")
    private int followingCount;

	public UserDto(User user) {
		this.username = user.getUsername();
		this.email = user.getEmail();
		this.rawPassword = null;
		this.picture = user.getPicture();
		this.createdAt = user.getCreatedAt();
		this.bio = user.getBio();
		this.publicProfile = user.getPublicProfile();
		this.profileWebsiteUrl = user.getProfileWebsiteUrl();
		this.aiQuotaRemaining = user.getAiQuotaRemaining();
		this.aiQuotaResetDate = user.getAiQuotaResetDate();
        this.followersCount = user.getFollowersCount();
        this.followingCount = user.getFollowingCount();
	}
}
