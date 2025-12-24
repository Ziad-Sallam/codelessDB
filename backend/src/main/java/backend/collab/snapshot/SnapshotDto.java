package backend.collab.snapshot;

import backend.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Data Transfer Object for bringing specific data in the session")
public class SnapshotDto {
    @Schema(description = "The name of the diagram", example = "Initial Schema")
    private String diagramName;

    @Schema(description = "The role of the user in the session")
    private Role role;
}
