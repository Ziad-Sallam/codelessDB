package backend.userDiagramManagement.dto;

import backend.entities.Diagram;
import backend.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class DiagramDto {
    private UUID id;
    private String name;
    private byte[] content;
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
