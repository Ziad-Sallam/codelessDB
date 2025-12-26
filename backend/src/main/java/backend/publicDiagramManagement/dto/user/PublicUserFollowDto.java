package backend.publicDiagramManagement.dto.user;

import backend.entities.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for high-level user following information")
public class PublicUserFollowDto {
    @Schema(description = "The display name of the user", example = "John Doe")
    private String name;
    @Schema(description = "The unique username of the user", example = "johndoe")
    private String username;
    @Schema(description = "The bio or description of the user", example = "Database enthusiast")
    private String bio;
    @Schema(description = "URL of the user's profile picture", example = "https://example.com/pic.jpg")
    private String picture;
    @Schema(description = "The number of public diagrams published by the user", example = "5")
    private long publicCount;   // number of published public diagrams
    @Schema(description = "The total number of stars received by the user's diagrams", example = "25")
    private long totalStars;
    @Schema(description = "The social score of the user", example = "150")
    private long score;
    @Schema(description = "The number of followers of the user", example = "10")
    private int followersCount;
    @Schema(description = "The number of users followed by the user", example = "15")
    private int followingCount;

    public static PublicUserFollowDto toDto(User user, Long publicCount, Long totalStars, Long score) {
        return PublicUserFollowDto.builder()
                .name(user.getPublicProfile() != null && !user.getPublicProfile().isBlank() ? user.getPublicProfile() : user.getUsername())
                .username(user.getUsername())
                .bio(user.getBio())
                .picture(user.getPicture())
                .publicCount(publicCount == null ? 0 : publicCount)
                .totalStars(totalStars == null ? 0 : totalStars)
                .score(score == null ? 0 : score)
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .build();
    }
}
