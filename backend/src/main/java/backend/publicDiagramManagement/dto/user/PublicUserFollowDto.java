package backend.publicDiagramManagement.dto.user;

import backend.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicUserFollowDto {

    private String name;
    private String username;
    private String bio;
    private String picture;
    private long publicCount;   // number of published public diagrams
    private long totalStars;
    private long score;
    private int followersCount;
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
