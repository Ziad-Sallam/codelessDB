package backend.userDiagramManagement.dto;

import backend.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for a diagram contributor")
public class ContributorDto {
    @Schema(description = "The display name of the contributor", example = "John Doe")
    private String name;

    @Schema(description = "URL of the contributor's profile picture", example = "https://example.com/pic.jpg")
    private String picture;

    @Schema(description = "The role of the contributor in the diagram (e.g., OWNER, EDITOR, VIEWER)")
    private Role role;
}
