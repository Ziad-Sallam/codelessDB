package backend.publicDiagramManagement.dto.user;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed Data Transfer Object for a public user profile")
public class PublicUserDto {
    @Schema(description = "The display name of the user", example = "John Doe")
    private String name;

    @Schema(description = "The unique username of the user", example = "johndoe")
    private String username;

    @Schema(description = "The bio or description of the user", example = "Database enthusiast")
    private String bio;

    @Schema(description = "URL of the user's profile picture", example = "https://example.com/pic.jpg")
    private String picture;

    @Schema(description = "The email address of the user (if public)", example = "john@example.com")
    private String email;

    @Schema(description = "Personal website URL of the user", example = "https://johndoe.com")
    private String url;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "The timestamp when the account was created")
    private LocalDateTime createdAt;

    @Schema(description = "The number of public diagrams published by the user", example = "5")
    private long publicCount;

    @Schema(description = "The total number of stars received by the user's diagrams", example = "25")
    private long totalStars;

    @JsonProperty("isFollowed")
    @Schema(description = "Whether the current user is following this user", example = "false")
    private boolean isFollowed;

    @Schema(description = "The number of followers of the user", example = "10")
    private int followersCount;

    @Schema(description = "The number of users followed by the user", example = "15")
    private int followingCount;

    @Schema(description = "The number of diagrams starred by the user", example = "8")
    private long starredCount;
}
