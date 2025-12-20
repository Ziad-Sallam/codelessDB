package backend.publicDiagramManagement.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import backend.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicUserInfoDto {

    private int id;
    private String name;
    private String username;
    private String bio;
    private String picture;

    private long publicCount;   // number of published public diagrams
    private long totalStars;    // sum of stars
    private long score;         // views + forks*2 + stars*3
    @JsonProperty("isFollowed")
    private boolean isFollowed;
    private int followersCount;
    private int followingCount;

    public static PublicUserInfoDto toDto(User user, Long publicCount, Long totalStars, Long score,  boolean isFollowed) {
        return PublicUserInfoDto.builder()
                .name(user.getPublicProfile() != null && !user.getPublicProfile().isBlank() ? user.getPublicProfile() : user.getUsername())
                .id(user.getId())
                .username(user.getUsername())
                .bio(user.getBio())
                .picture(user.getPicture())
                .publicCount(publicCount == null ? 0 : publicCount)
                .totalStars(totalStars == null ? 0 : totalStars)
                .score(score == null ? 0 : score)
                .isFollowed(isFollowed)
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .build();
    }
}
