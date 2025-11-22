package backend.userDiagramManagement.dto;

import backend.entities.Diagram;
import backend.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagramInfoDto {
    private UUID id;
    private String name;
    private String thumbnail;
    private Date createdAt;
    private Date lastModified;
    private Role role;
    private List<Contributor> contributors;

    public record Contributor(String name, Role role) {}

    public static DiagramInfoDto toDto(Diagram diagram, Role role, List<Contributor> contributors) {
        return DiagramInfoDto
                .builder()
                .id(diagram.getId())
                .name(diagram.getName())
                .thumbnail(diagram.getThumbnail())
                .createdAt(diagram.getCreatedAt())
                .lastModified(diagram.getLastModified())
                .role(role)
                .contributors(contributors)
                .build();
    }

}
