package backend.userDiagramManagement.dto;

import java.util.UUID;

import backend.entities.Diagram;
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
@Schema(description = "Detailed Data Transfer Object for a database diagram")
public class DiagramDto {
    @Schema(description = "The unique identifier of the diagram", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "The name of the diagram", example = "E-commerce System")
    private String name;

    @Schema(description = "The binary or JSON content of the diagram")
    private byte[] content;

    @Schema(description = "The role of the current user for this diagram")
    private Role role;

    public static DiagramDto toDto(Diagram diagram, Role role) {
        return DiagramDto
                .builder()
                .id(diagram.getId())
                .name(diagram.getName())
                .content(diagram.getContent())
                .role(role)
                .build();
    }
}
