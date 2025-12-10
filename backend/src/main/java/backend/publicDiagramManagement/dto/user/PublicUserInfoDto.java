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
public class PublicUserInfoDto {

    private int id;
    private String username;
    private String bio;
    private String picture;

    private long publicCount;   // number of published public diagrams
    private long totalStars;    // sum of stars
    private long score;         // views + forks*2 + stars*3

    public static PublicUserInfoDto toDto(User user, Long publicCount, Long totalStars, Long score) {
        return PublicUserInfoDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .bio(user.getBio())
                .picture(user.getPicture())
                .publicCount(publicCount == null ? 0 : publicCount)
                .totalStars(totalStars == null ? 0 : totalStars)
                .score(score == null ? 0 : score)
                .build();
    }
}
