package backend.userDiagramManagement.dto;

import backend.entities.Diagram;
import backend.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class DiagramDto {
    private UUID id;
    private String name;
    private String content;
    private byte[] thumbnail;
    private Date createdAt;
    private Date lastModified;
    private String role;

    public static DiagramDto toDto(Diagram diagram, Role role) {
        return DiagramDto
                .builder()
                .id(diagram.getId())
                .name(diagram.getName())
                .content(diagram.getContent())
                .thumbnail(diagram.getThumbnail())
                .thumbnail(diagram.getThumbnail())
                .createdAt(diagram.getCreatedAt())
                .lastModified(diagram.getLastModified())
                .role(role.name())
                .build();
    }
}

