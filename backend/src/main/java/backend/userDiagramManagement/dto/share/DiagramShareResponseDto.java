package backend.userDiagramManagement.dto.share;

import backend.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Response object for diagram sharing result")
public class DiagramShareResponseDto {
    @Schema(description = "Result message", example = "Diagram shared successfully")
    private String message;

    @Schema(description = "The username the diagram was shared with", example = "johndoe")
    private String sharedWith;

    @Schema(description = "The role assigned to the user")
    private Role role;

    @Schema(description = "URL of the shared user's profile picture", example = "https://example.com/pic.jpg")
    private String picture;
}
