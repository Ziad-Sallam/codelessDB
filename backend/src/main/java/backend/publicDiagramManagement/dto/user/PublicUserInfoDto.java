package backend.publicDiagramManagement.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PublicUserInfoDto {
    String username;
    String picture;
    String bio;
    int followers;
    int following;
    int publicDiagrams;
}
