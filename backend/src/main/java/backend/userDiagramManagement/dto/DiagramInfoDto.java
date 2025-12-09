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
    private UUID diagramId;
    private String name;
    private String thumbnail;
    private Date createdAt;
    private Date lastModified;
    private Role role;
    private List<ContributorDto> contributorDtos;

    public static DiagramInfoDto toDto(Diagram diagram, Role role, List<ContributorDto> contributorDtos) {
        return DiagramInfoDto
                .builder()
                .role(role)
                .diagramId(diagram.getId())
                .name(diagram.getName())
                .thumbnail(diagram.getThumbnail())
                .createdAt(diagram.getCreatedAt())
                .lastModified(diagram.getLastModified())
                .contributorDtos(contributorDtos)
                .build();
    }
}
