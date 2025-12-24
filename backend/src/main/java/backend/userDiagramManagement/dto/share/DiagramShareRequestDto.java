package backend.userDiagramManagement.dto.share;

import backend.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Request object for sharing or unsharing a diagram with another user")
public class DiagramShareRequestDto {
    @Schema(description = "The username of the recipient", example = "johndoe")
    private String toUserName;

    @Schema(description = "The role to assign to the user")
    private Role role;

    @Schema(description = "Whether to remove the user's access (unshare)", example = "false")
    private boolean delete;
}
