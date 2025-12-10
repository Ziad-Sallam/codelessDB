package backend.publicDiagramManagement.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserDto {
    private String name;
    private String username;
    private String bio;
    private String picture;
    private String email;
    private String url;
    private long publicCount;
    private long totalStars;
}
