package backend.userDiagramManagement.dto;

import backend.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContributorDto {
    private String name;
    private String picture;
    private Role role;
}
